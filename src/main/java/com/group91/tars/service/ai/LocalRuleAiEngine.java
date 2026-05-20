package com.group91.tars.service.ai;

import com.group91.tars.model.JobPosting;
import com.group91.tars.model.TAProfile;
import com.group91.tars.model.WorkloadSummary;
import com.group91.tars.model.ai.AiCandidateSummary;
import com.group91.tars.model.ai.AiFitResult;
import com.group91.tars.model.ai.AiWorkloadAdvice;
import com.group91.tars.service.TarsService;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class LocalRuleAiEngine {
    public SkillMatch evaluateFit(TAProfile profile, JobPosting job, String cvText, String extraCandidateText) {
        Set<String> candidateTokens = new LinkedHashSet<String>();
        candidateTokens.addAll(tokenize(profile == null ? null : profile.getSkills()));
        candidateTokens.addAll(tokenize(cvText));
        candidateTokens.addAll(tokenize(extraCandidateText));

        List<String> requiredSkills = tokenize(job == null ? null : job.getSkills());
        List<String> matched = new ArrayList<String>();
        List<String> missing = new ArrayList<String>();

        for (String jobSkill : requiredSkills) {
            if (isBlank(jobSkill)) {
                continue;
            }
            if (matchesAny(jobSkill, candidateTokens)) {
                matched.add(jobSkill);
            } else {
                missing.add(jobSkill);
            }
        }

        int score = requiredSkills.isEmpty() ? 0 : (int) Math.round((matched.size() * 100.0) / requiredSkills.size());
        return new SkillMatch(score, matched, missing);
    }

    public AiFitResult buildLocalFit(TAProfile profile, JobPosting job, String cvText) {
        SkillMatch match = evaluateFit(profile, job, cvText, null);
        return AiFitResult.local(match.getScore(), match.getMatchedSkills(), match.getMissingSkills(),
            buildCvEvidence(match, cvText), buildFitAdvice(match));
    }

    public AiCandidateSummary buildLocalCandidateSummary(TAProfile profile, JobPosting job, String cvText,
                                                         String applicationText, int acceptedJobs) {
        SkillMatch match = evaluateFit(profile, job, cvText, applicationText);
        AiCandidateSummary summary = new AiCandidateSummary();
        summary.setScore(match.getScore());
        summary.setMatchedSkills(match.getMatchedSkills());
        summary.setMissingSkills(match.getMissingSkills());
        summary.setCvEvidence(buildCvEvidence(match, cvText));
        summary.setAcceptedJobs(acceptedJobs);
        summary.setWorkloadRisk(workloadRisk(acceptedJobs));
        summary.setShortlistRecommendation(shortlistRecommendation(match.getScore(), summary.getWorkloadRisk()));
        summary.setAdvice(buildCandidateAdvice(summary));
        summary.setSourceMode("local");
        return summary;
    }

    public List<AiWorkloadAdvice> buildWorkloadAdvice(List<WorkloadSummary> summaries) {
        List<AiWorkloadAdvice> advice = new ArrayList<AiWorkloadAdvice>();
        if (summaries == null) {
            return advice;
        }
        for (WorkloadSummary summary : summaries) {
            AiWorkloadAdvice item = new AiWorkloadAdvice();
            item.setTaId(summary.getTaId());
            item.setTaName(summary.getTaName());
            item.setAcceptedCount(summary.getAcceptedCount());
            item.setAcceptedModules(summary.getAcceptedModules());
            item.setWorkloadRisk(workloadRisk(summary.getAcceptedCount()));
            item.setAdvice(buildWorkloadMessage(summary.getAcceptedCount(), item.getWorkloadRisk()));
            item.setSourceMode("local");
            advice.add(item);
        }
        return advice;
    }

    public String workloadRisk(int acceptedJobs) {
        if (acceptedJobs >= TarsService.MAX_ACCEPTED_JOBS) {
            return "at_cap";
        }
        if (acceptedJobs == TarsService.MAX_ACCEPTED_JOBS - 1) {
            return "caution";
        }
        return "low";
    }

    public String shortlistRecommendation(int score, String workloadRisk) {
        if (!"at_cap".equals(workloadRisk) && score >= 75) {
            return "Strong";
        }
        if (!"at_cap".equals(workloadRisk) && score >= 50) {
            return "Consider";
        }
        return "Weak";
    }

    public String buildCvEvidence(SkillMatch match, String cvText) {
        if (isCvUnavailable(cvText)) {
            return cvText;
        }
        if (match.getMatchedSkills().isEmpty()) {
            return "The PDF CV was parsed, but no required skill terms were detected by the local rule engine.";
        }
        return "The PDF CV/profile evidence mentions: " + join(match.getMatchedSkills()) + ".";
    }

    public String buildFitAdvice(SkillMatch match) {
        if (match.getMissingSkills().isEmpty()) {
            return "This profile appears to match the required skills. The TA should still tailor the application note to the module responsibilities.";
        }
        return "The TA should address these gaps before applying or explain related experience: "
            + join(match.getMissingSkills()) + ".";
    }

    private String buildCandidateAdvice(AiCandidateSummary summary) {
        if ("at_cap".equals(summary.getWorkloadRisk())) {
            return "Candidate has reached the workload cap. Do not accept additional assignments without reducing workload.";
        }
        if ("Strong".equals(summary.getShortlistRecommendation())) {
            return "Candidate is a strong skills match. Review the application note and make a human hiring decision.";
        }
        if ("Consider".equals(summary.getShortlistRecommendation())) {
            return "Candidate may be suitable, but missing skills should be checked during review.";
        }
        return "Candidate has a weak deterministic match or workload concern. Review carefully before deciding.";
    }

    private String buildWorkloadMessage(int acceptedCount, String risk) {
        if ("at_cap".equals(risk)) {
            return "At the workload cap (" + acceptedCount + "/" + TarsService.MAX_ACCEPTED_JOBS
                + "). Avoid assigning more accepted jobs.";
        }
        if ("caution".equals(risk)) {
            return "Near the workload cap (" + acceptedCount + "/" + TarsService.MAX_ACCEPTED_JOBS
                + "). Use caution before accepting another application.";
        }
        return "Normal workload (" + acceptedCount + "/" + TarsService.MAX_ACCEPTED_JOBS
            + "). Capacity is available.";
    }

    private boolean matchesAny(String jobSkill, Set<String> candidateTokens) {
        for (String candidateToken : candidateTokens) {
            if (jobSkill.equals(candidateToken) || candidateToken.contains(jobSkill) || jobSkill.contains(candidateToken)) {
                return true;
            }
        }
        return false;
    }

    private List<String> tokenize(String text) {
        List<String> tokens = new ArrayList<String>();
        if (isBlank(text)) {
            return tokens;
        }
        String[] parts = text.toLowerCase(Locale.ENGLISH).split("[,;\\s]+");
        Set<String> unique = new LinkedHashSet<String>();
        for (String part : parts) {
            String token = part.trim();
            if (!isBlank(token)) {
                unique.add(token);
            }
        }
        tokens.addAll(unique);
        return tokens;
    }

    private boolean isCvUnavailable(String cvText) {
        return isBlank(cvText)
            || "CV not available.".equals(cvText)
            || "CV analysis supports PDF files only.".equals(cvText)
            || "CV could not be parsed.".equals(cvText);
    }

    private String join(List<String> values) {
        return values == null || values.isEmpty() ? "-" : String.join(", ", values);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public static class SkillMatch {
        private final int score;
        private final List<String> matchedSkills;
        private final List<String> missingSkills;

        public SkillMatch(int score, List<String> matchedSkills, List<String> missingSkills) {
            this.score = score;
            this.matchedSkills = matchedSkills;
            this.missingSkills = missingSkills;
        }

        public int getScore() {
            return score;
        }

        public List<String> getMatchedSkills() {
            return matchedSkills;
        }

        public List<String> getMissingSkills() {
            return missingSkills;
        }
    }
}
