package com.group91.tars.model;

import java.util.ArrayList;
import java.util.List;

public class WorkloadSummary {
    private String taId;
    private String taName;
    private List<String> acceptedModules = new ArrayList<String>();
    private int acceptedCount;
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

    public boolean isOverloadFlag() {
        return overloadFlag;
    }

    public void setOverloadFlag(boolean overloadFlag) {
        this.overloadFlag = overloadFlag;
    }
}
