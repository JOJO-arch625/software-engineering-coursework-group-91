package com.group91.tars.model.ai;

import java.util.ArrayList;
import java.util.List;

public class AiCandidateSummary {
    private int score;
    private List<String> matchedSkills = new ArrayList<String>();
    private List<String> missingSkills = new ArrayList<String>();
    private String cvEvidence;
    private int acceptedJobs;
    private String workloadRisk;
    private String shortlistRecommendation;
    private String advice;
    private String sourceMode;
    private String errorMessage;

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

    public int getAcceptedJobs() {
        return acceptedJobs;
    }

    public void setAcceptedJobs(int acceptedJobs) {
        this.acceptedJobs = acceptedJobs;
    }

    public String getWorkloadRisk() {
        return workloadRisk;
    }

    public void setWorkloadRisk(String workloadRisk) {
        this.workloadRisk = workloadRisk;
    }

    public String getShortlistRecommendation() {
        return shortlistRecommendation;
    }

    public void setShortlistRecommendation(String shortlistRecommendation) {
        this.shortlistRecommendation = shortlistRecommendation;
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
