package com.group91.tars.service;

import com.group91.tars.model.JobPosting;
import com.group91.tars.model.JobRecommendation;
import com.group91.tars.model.TAProfile;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Tests for position matching and skill fit algorithms.
 */
public class PositionMatchingTest {

    private TarsService service;

    @Before
    public void setUp() {
        service = TarsService.getInstance();
    }

    // ===================== Fit Score Calculation =====================

    @Test
    public void calculateFitScore_returnsZeroToHundred() {
        int score = service.calculateFitScore("ta-1", "job-1");
        assertTrue("Score should be between 0-100, got: " + score,
                   score >= 0 && score <= 100);
    }

    @Test
    public void calculateFitScore_returnsConsistentScore() {
        int score1 = service.calculateFitScore("ta-1", "job-2");
        int score2 = service.calculateFitScore("ta-1", "job-2");
        assertEquals("Scores should be consistent", score1, score2);
    }

    @Test
    public void calculateFitScore_handlesNonExistentTa() {
        int score = service.calculateFitScore("non-existent-ta", "job-1");
        assertTrue("Score should be between 0-100", score >= 0 && score <= 100);
    }

    @Test
    public void calculateFitScore_handlesNonExistentJob() {
        int score = service.calculateFitScore("ta-1", "non-existent-job");
        assertTrue("Score should be between 0-100", score >= 0 && score <= 100);
    }

    // ===================== Missing Skills =====================

    @Test
    public void getMissingSkills_returnsNonNull() {
        List<String> missing = service.getMissingSkillsPublic("ta-1", "job-1");
        assertNotNull("Missing skills list should not be null", missing);
    }

    @Test
    public void getMissingSkills_handlesNonExistentTa() {
        List<String> missing = service.getMissingSkillsPublic("non-existent", "job-1");
        assertNotNull("Should return empty list, not null", missing);
    }

    @Test
    public void getMissingSkills_handlesNonExistentJob() {
        List<String> missing = service.getMissingSkillsPublic("ta-1", "non-existent");
        assertNotNull("Should return empty list, not null", missing);
    }

    // ===================== Job Recommendations =====================

    @Test
    public void getRecommendedJobsForTa_returnsJobsAboveThreshold() {
        List<JobRecommendation> recommendations = service.getRecommendedJobsForTa("ta-1");
        assertNotNull("Recommendations should not be null", recommendations);

        for (JobRecommendation rec : recommendations) {
            assertTrue("Recommendation should meet 60% threshold, got: " + rec.getMatchRate(),
                       rec.getMatchRate() >= 60);
        }
    }

    @Test
    public void getRecommendedJobsForTa_returnsEmpty_forNonExistentTa() {
        List<JobRecommendation> recommendations = service.getRecommendedJobsForTa("non-existent-ta");
        assertNotNull("Should return empty list, not null", recommendations);
    }

    @Test
    public void getRecommendedJobsForTa_includesMatchDetails() {
        List<JobRecommendation> recommendations = service.getRecommendedJobsForTa("ta-1");

        if (!recommendations.isEmpty()) {
            JobRecommendation rec = recommendations.get(0);
            assertNotNull("Job should not be null", rec.getJob());
            assertTrue("Match count should be >= 0", rec.getMatchedCount() >= 0);
            assertTrue("Total required should be >= 0", rec.getTotalRequiredCount() >= 0);
            assertTrue("Match rate should be 0-100", rec.getMatchRate() >= 0 && rec.getMatchRate() <= 100);
        }
    }

    @Test
    public void getRecommendedJobsForTa_matchRateCalculation() {
        List<JobRecommendation> recommendations = service.getRecommendedJobsForTa("ta-1");

        for (JobRecommendation rec : recommendations) {
            if (rec.getTotalRequiredCount() > 0) {
                int expectedRate = (rec.getMatchedCount() * 100) / rec.getTotalRequiredCount();
                assertTrue("Match rate mismatch: expected ~" + expectedRate + ", got " + rec.getMatchRate(),
                           Math.abs(rec.getMatchRate() - expectedRate) <= 1 || rec.getMatchRate() == 100);
            }
        }
    }

    @Test
    public void getRecommendedJobsForTa_sortsByMatchRate() {
        List<JobRecommendation> recommendations = service.getRecommendedJobsForTa("ta-1");

        if (recommendations.size() > 1) {
            for (int i = 0; i < recommendations.size() - 1; i++) {
                int current = recommendations.get(i).getMatchRate();
                int next = recommendations.get(i + 1).getMatchRate();
                assertTrue("Recommendations should be sorted descending by match rate", current >= next);
            }
        }
    }
}
