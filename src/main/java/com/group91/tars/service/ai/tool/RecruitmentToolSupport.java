package com.group91.tars.service.ai.tool;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.group91.tars.model.ApplicationRecord;
import com.group91.tars.model.JobPosting;
import com.group91.tars.model.TAProfile;
import com.group91.tars.model.UserAccount;
import com.group91.tars.service.TarsService;
import com.group91.tars.storage.JsonDataStore;

import java.util.List;

class RecruitmentToolSupport {
    private final JsonDataStore store = JsonDataStore.getInstance();
    private final UserAccount currentUser;

    RecruitmentToolSupport(UserAccount currentUser) {
        this.currentUser = currentUser;
    }

    JsonDataStore getStore() {
        return store;
    }

    UserAccount getCurrentUser() {
        return currentUser;
    }

    boolean canReadTa(String taId) {
        if (currentUser == null) {
            return false;
        }
        if (TarsService.ROLE_ADMIN.equals(currentUser.getRole())) {
            return true;
        }
        if (TarsService.ROLE_TA.equals(currentUser.getRole())) {
            return taId != null && taId.equals(currentUser.getLinkedId());
        }
        return TarsService.ROLE_MO.equals(currentUser.getRole()) && hasApplicationForOwnJob(taId);
    }

    boolean canReadCv(String taId) {
        return canReadTa(taId);
    }

    boolean canReadJob(String jobId) {
        if (currentUser == null) {
            return false;
        }
        if (TarsService.ROLE_ADMIN.equals(currentUser.getRole())
            || TarsService.ROLE_TA.equals(currentUser.getRole())) {
            return true;
        }
        JobPosting job = findJob(jobId);
        return job != null && TarsService.ROLE_MO.equals(currentUser.getRole())
            && currentUser.getLinkedId().equals(job.getMoId());
    }

    boolean canListOpenJobs() {
        return currentUser != null && (TarsService.ROLE_TA.equals(currentUser.getRole())
            || TarsService.ROLE_ADMIN.equals(currentUser.getRole()));
    }

    boolean canListApplicants(String jobId) {
        if (currentUser == null) {
            return false;
        }
        if (TarsService.ROLE_ADMIN.equals(currentUser.getRole())) {
            return true;
        }
        JobPosting job = findJob(jobId);
        return job != null && TarsService.ROLE_MO.equals(currentUser.getRole())
            && currentUser.getLinkedId().equals(job.getMoId());
    }

    TAProfile findProfile(String taId) {
        for (TAProfile profile : store.loadProfiles()) {
            if (profile.getId().equals(taId)) {
                return profile;
            }
        }
        return null;
    }

    JobPosting findJob(String jobId) {
        for (JobPosting job : store.loadJobs()) {
            if (job.getId().equals(jobId)) {
                return job;
            }
        }
        return null;
    }

    ApplicationRecord findApplication(String applicationId) {
        for (ApplicationRecord application : store.loadApplications()) {
            if (application.getId().equals(applicationId)) {
                return application;
            }
        }
        return null;
    }

    int countAcceptedJobs(String taId) {
        int count = 0;
        for (ApplicationRecord application : store.loadApplications()) {
            if (taId != null && taId.equals(application.getTaId()) && "Accepted".equals(application.getStatus())) {
                count++;
            }
        }
        return count;
    }

    String workloadRisk(int acceptedJobs) {
        if (acceptedJobs >= TarsService.MAX_ACCEPTED_JOBS) {
            return "at_cap";
        }
        if (acceptedJobs == TarsService.MAX_ACCEPTED_JOBS - 1) {
            return "caution";
        }
        return "low";
    }

    String workloadMessage(int acceptedJobs, String risk) {
        if ("at_cap".equals(risk)) {
            return "This TA is at the workload cap.";
        }
        if ("caution".equals(risk)) {
            return "This TA is near workload capacity.";
        }
        return "This TA has normal workload capacity.";
    }

    String requiredString(JsonObject arguments, String key) {
        if (arguments == null || !arguments.has(key) || arguments.get(key).isJsonNull()
            || arguments.get(key).getAsString().trim().isEmpty()) {
            return null;
        }
        return arguments.get(key).getAsString().trim();
    }

    JsonObject schema(String... required) {
        JsonObject schema = new JsonObject();
        schema.addProperty("type", "object");
        JsonObject properties = new JsonObject();
        JsonArray requiredArray = new JsonArray();
        for (String name : required) {
            JsonObject property = new JsonObject();
            property.addProperty("type", "string");
            properties.add(name, property);
            requiredArray.add(name);
        }
        schema.add("properties", properties);
        schema.add("required", requiredArray);
        return schema;
    }

    JsonArray toJsonArray(List<String> values) {
        JsonArray array = new JsonArray();
        if (values != null) {
            for (String value : values) {
                array.add(value);
            }
        }
        return array;
    }

    private boolean hasApplicationForOwnJob(String taId) {
        if (currentUser == null || taId == null || !TarsService.ROLE_MO.equals(currentUser.getRole())) {
            return false;
        }
        for (ApplicationRecord application : store.loadApplications()) {
            if (!taId.equals(application.getTaId())) {
                continue;
            }
            JobPosting job = findJob(application.getJobId());
            if (job != null && currentUser.getLinkedId().equals(job.getMoId())) {
                return true;
            }
        }
        return false;
    }
}
