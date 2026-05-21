package com.group91.tars.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Computed (non-persisted) summary of a TA's current workload in the ISTARS system.
 * Aggregated from accepted applications, this model is used by the Admin workload
 * dashboard to display each TA's assignment count and overload status.
 */
public class WorkloadSummary {
    /** TA profile identifier (e.g. "ta-1"). */
    private String taId;
    /** TA full name for display. */
    private String taName;
    /** List of module codes for the TA's accepted jobs. */
    private List<String> acceptedModules = new ArrayList<String>();
    /** Number of accepted job assignments. */
    private int acceptedCount;
    /** Sum of accepted job weekly hours. */
    private int totalWeeklyHours;
    /** Whether the TA has reached or exceeded the workload cap. */
    private boolean overloadFlag;

    public String getTaId() {
        return taId;
    }

    public void setTaId(String taId) {
        this.taId = taId;
    }

    public String getTaName() {
        return taName;
    }

    public void setTaName(String taName) {
        this.taName = taName;
    }

    public List<String> getAcceptedModules() {
        return acceptedModules;
    }

    public void setAcceptedModules(List<String> acceptedModules) {
        this.acceptedModules = acceptedModules;
    }

    public int getAcceptedCount() {
        return acceptedCount;
    }

    public void setAcceptedCount(int acceptedCount) {
        this.acceptedCount = acceptedCount;
    }

    public int getTotalWeeklyHours() {
        return totalWeeklyHours;
    }

    public void setTotalWeeklyHours(int totalWeeklyHours) {
        this.totalWeeklyHours = totalWeeklyHours;
    }

    public boolean isOverloadFlag() {
        return overloadFlag;
    }

    public void setOverloadFlag(boolean overloadFlag) {
        this.overloadFlag = overloadFlag;
    }
}
