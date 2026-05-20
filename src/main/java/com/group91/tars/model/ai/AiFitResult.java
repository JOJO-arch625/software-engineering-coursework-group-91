package com.group91.tars.model.ai;

import java.util.ArrayList;
import java.util.List;

public class AiFitResult {
    private int score;
    private List<String> matchedSkills = new ArrayList<String>();
    private List<String> missingSkills = new ArrayList<String>();
    private String cvEvidence;
    private String advice;
    private String sourceMode;
    private String errorMessage;

    public AiFitResult() {
    }

    public static AiFitResult local(int score, List<String> matchedSkills, List<String> missingSkills,
                                    String cvEvidence, String advice) {
        AiFitResult result = base(score, matchedSkills, missingSkills, cvEvidence, advice);
        result.setSourceMode("local");
        return result;
    }

    public static AiFitResult llm(int score, List<String> matchedSkills, List<String> missingSkills,
                                  String cvEvidence, String advice) {
        AiFitResult result = base(score, matchedSkills, missingSkills, cvEvidence, advice);
        result.setSourceMode("llm");
        return result;
    }

    public static AiFitResult error(int score, List<String> matchedSkills, List<String> missingSkills,
                                    String errorMessage) {
        AiFitResult result = base(score, matchedSkills, missingSkills,
            "CV evidence is unavailable because the AI request could not be completed.",
            "Please review the deterministic skill match and make a human decision.");
        result.setSourceMode("error");
        result.setErrorMessage(errorMessage);
        return result;
    }

    private static AiFitResult base(int score, List<String> matchedSkills, List<String> missingSkills,
                                    String cvEvidence, String advice) {
        AiFitResult result = new AiFitResult();
        result.setScore(score);
        result.setMatchedSkills(matchedSkills);
        result.setMissingSkills(missingSkills);
        result.setCvEvidence(cvEvidence);
        result.setAdvice(advice);
        return result;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = Math.max(0, Math.min(100, score));
    }

    public List<String> getMatchedSkills() {
        return matchedSkills;
    }

    public void setMatchedSkills(List<String> matchedSkills) {
        this.matchedSkills = matchedSkills == null ? new ArrayList<String>() : matchedSkills;
    }

    public List<String> getMissingSkills() {
        return missingSkills;
    }

    public void setMissingSkills(List<String> missingSkills) {
        this.missingSkills = missingSkills == null ? new ArrayList<String>() : missingSkills;
    }

    public String getCvEvidence() {
        return cvEvidence;
    }

    public void setCvEvidence(String cvEvidence) {
        this.cvEvidence = cvEvidence;
    }

    public String getAdvice() {
        return advice;
    }

    public void setAdvice(String advice) {
        this.advice = advice;
    }

    public String getSourceMode() {
        return sourceMode;
    }

    public void setSourceMode(String sourceMode) {
        this.sourceMode = sourceMode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
