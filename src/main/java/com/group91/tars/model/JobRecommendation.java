package com.group91.tars.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Explainable TA-side job recommendation based on skill overlap.
 */
public class JobRecommendation {
    private JobPosting job;
    private int matchedCount;
    private int totalRequiredCount;
    private int matchRate;
    private List<String> matchedSkills = new ArrayList<String>();

    public JobPosting getJob() {
        return job;
    }

    public void setJob(JobPosting job) {
        this.job = job;
    }

    public int getMatchedCount() {
        return matchedCount;
    }

    public void setMatchedCount(int matchedCount) {
        this.matchedCount = matchedCount;
    }

    public int getTotalRequiredCount() {
        return totalRequiredCount;
    }

    public void setTotalRequiredCount(int totalRequiredCount) {
        this.totalRequiredCount = totalRequiredCount;
    }

    public int getMatchRate() {
        return matchRate;
    }

    public void setMatchRate(int matchRate) {
        this.matchRate = matchRate;
    }

    public List<String> getMatchedSkills() {
        return matchedSkills;
    }

    public void setMatchedSkills(List<String> matchedSkills) {
        this.matchedSkills = matchedSkills;
    }
}
