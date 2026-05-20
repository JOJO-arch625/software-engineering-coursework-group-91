package com.group91.tars.model.ai;

import java.util.ArrayList;
import java.util.List;

public class AiWorkloadAdvice {
    private String taId;
    private String taName;
    private int acceptedCount;
    private List<String> acceptedModules = new ArrayList<String>();
    private String workloadRisk;
    private String advice;
    private String sourceMode;

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

    public int getAcceptedCount() {
        return acceptedCount;
    }

    public void setAcceptedCount(int acceptedCount) {
        this.acceptedCount = acceptedCount;
    }

    public List<String> getAcceptedModules() {
        return acceptedModules;
    }

    public void setAcceptedModules(List<String> acceptedModules) {
        this.acceptedModules = acceptedModules == null ? new ArrayList<String>() : acceptedModules;
    }

    public String getWorkloadRisk() {
        return workloadRisk;
    }

    public void setWorkloadRisk(String workloadRisk) {
        this.workloadRisk = workloadRisk;
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
}
