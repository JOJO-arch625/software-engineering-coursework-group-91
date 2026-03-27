package com.group91.tars.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.group91.tars.model.ApplicationRecord;
import com.group91.tars.model.JobPosting;
import com.group91.tars.model.TAProfile;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class JsonDataStore {
    private static final JsonDataStore INSTANCE = new JsonDataStore();

    private static final Type PROFILE_LIST_TYPE = new TypeToken<List<TAProfile>>() { }.getType();
    private static final Type JOB_LIST_TYPE = new TypeToken<List<JobPosting>>() { }.getType();
    private static final Type APPLICATION_LIST_TYPE = new TypeToken<List<ApplicationRecord>>() { }.getType();

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Path rootDirectory = Paths.get(System.getProperty("user.dir"));
    private final Path dataDirectory = rootDirectory.resolve("data");
    private final Path uploadDirectory = rootDirectory.resolve(Paths.get("uploads", "cv"));
    private final Path profilesFile = dataDirectory.resolve("ta-profiles.json");
    private final Path jobsFile = dataDirectory.resolve("job-postings.json");
    private final Path applicationsFile = dataDirectory.resolve("applications.json");

    private JsonDataStore() {
    }

    public static JsonDataStore getInstance() {
        return INSTANCE;
    }

    public void initialize() {
        try {
            Files.createDirectories(dataDirectory);
            Files.createDirectories(uploadDirectory);
            ensureSeedProfiles();
            ensureSeedJobs();
            ensureSeedApplications();
            ensureSampleCv();
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to initialise local JSON storage.", exception);
        }
    }

    public List<TAProfile> loadProfiles() {
        return readList(profilesFile, PROFILE_LIST_TYPE);
    }

    public void saveProfiles(List<TAProfile> profiles) {
        writeList(profilesFile, profiles);
    }

    public List<JobPosting> loadJobs() {
        return readList(jobsFile, JOB_LIST_TYPE);
    }

    public void saveJobs(List<JobPosting> jobs) {
        writeList(jobsFile, jobs);
    }

    public List<ApplicationRecord> loadApplications() {
        return readList(applicationsFile, APPLICATION_LIST_TYPE);
    }

    public void saveApplications(List<ApplicationRecord> applications) {
        writeList(applicationsFile, applications);
    }

    public String saveCvFile(InputStream inputStream, String fileName) throws IOException {
        String safeFileName = fileName.replaceAll("[^A-Za-z0-9._-]", "_");
        Path destination = uploadDirectory.resolve(safeFileName);
        Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);
        return Paths.get("uploads", "cv", safeFileName).toString().replace('\\', '/');
    }

    private <T> List<T> readList(Path path, Type type) {
        if (!Files.exists(path)) {
            return new ArrayList<T>();
        }
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            List<T> data = gson.fromJson(reader, type);
            return data == null ? new ArrayList<T>() : data;
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to read JSON file: " + path, exception);
        }
    }

    private void writeList(Path path, Object data) {
        try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            gson.toJson(data, writer);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to write JSON file: " + path, exception);
        }
    }

    private void ensureSeedProfiles() throws IOException {
        if (Files.exists(profilesFile) && Files.size(profilesFile) > 0) {
            return;
        }

        List<TAProfile> profiles = new ArrayList<TAProfile>();
        profiles.add(createProfile("ta-1", "Yuyanchen Long", "231224653",
            "yuyanchen.long@qmul.ac.uk", "+86 188 0000 0000",
            "Java, Python, VHDL, Object-Oriented Programming",
            "Available on Tuesday afternoon, Thursday evening, and Friday morning.",
            "uploads/cv/YuyanchenLong_CV.pdf"));
        profiles.add(createProfile("ta-2", "Ming Li", "231224601",
            "ming.li@qmul.ac.uk", "+86 177 0000 0001",
            "Python, Lab support, Data structures",
            "Available on Wednesday afternoon and Friday afternoon.",
            "uploads/cv/MingLi_CV.pdf"));
        profiles.add(createProfile("ta-3", "Siyu Chen", "231224612",
            "siyu.chen@qmul.ac.uk", "+86 177 0000 0002",
            "Digital logic, Simulation, Student support",
            "Available on Monday morning and Thursday afternoon.",
            "uploads/cv/SiyuChen_CV.pdf"));

        writeList(profilesFile, profiles);
    }

    private void ensureSeedJobs() throws IOException {
        if (Files.exists(jobsFile) && Files.size(jobsFile) > 0) {
            return;
        }

        List<JobPosting> jobs = new ArrayList<JobPosting>();
        jobs.add(createJob("job-1", "mo-1", "EIE3320", "Object-Oriented Programming TA",
            "Java, OOP, debugging", "Strong Java syntax, classes, arrays, exception handling, and lab support communication.",
            "6 hours / week", "2026-04-10", "Open",
            "Support weekly labs and guide students through Java exercises."));
        jobs.add(createJob("job-2", "mo-1", "EIE2105", "Digital Systems TA",
            "VHDL, Boolean logic, simulation", "Basic circuit design, VHDL syntax, waveform debugging, and hardware fundamentals.",
            "5 hours / week", "2026-04-11", "Open",
            "Assist digital systems lab sessions and hardware simulation support."));
        jobs.add(createJob("job-3", "mo-1", "ECS5001", "Data Analytics TA",
            "Python, pandas, plotting", "Python basics, data processing, notebook workflows, and problem explanation skills.",
            "4 hours / week", "2026-04-09", "Closed",
            "Help students with Python notebooks and analytics exercises."));

        writeList(jobsFile, jobs);
    }

    private void ensureSeedApplications() throws IOException {
        if (Files.exists(applicationsFile) && Files.size(applicationsFile) > 0) {
            return;
        }

        List<ApplicationRecord> applications = new ArrayList<ApplicationRecord>();
        applications.add(createApplication("app-1", "job-2", "ta-1", "Priority 1", "Under Review",
            "MO reviewing VHDL experience.", "2026-03-24 10:00"));
        applications.add(createApplication("app-2", "job-3", "ta-1", "Priority 2", "Rejected",
            "Role filled. Transparent rejection shown to TA.", "2026-03-23 18:30"));
        applications.add(createApplication("app-3", "job-1", "ta-2", "Priority 1", "Accepted",
            "Accepted for weekly Java lab support.", "2026-03-22 09:00"));
        applications.add(createApplication("app-4", "job-2", "ta-3", "Priority 1", "Accepted",
            "Accepted for digital systems support.", "2026-03-22 09:10"));
        applications.add(createApplication("app-5", "job-1", "ta-3", "Priority 2", "Accepted",
            "Accepted for additional OOP sessions.", "2026-03-23 14:20"));
        applications.add(createApplication("app-6", "job-3", "ta-3", "Priority 3", "Accepted",
            "Accepted before the role was closed.", "2026-03-21 11:45"));

        writeList(applicationsFile, applications);
    }

    private void ensureSampleCv() throws IOException {
        createPlaceholderCv("YuyanchenLong_CV.pdf");
        createPlaceholderCv("MingLi_CV.pdf");
        createPlaceholderCv("SiyuChen_CV.pdf");
    }

    private void createPlaceholderCv(String fileName) throws IOException {
        Path path = uploadDirectory.resolve(fileName);
        if (!Files.exists(path)) {
            Files.write(path, ("Placeholder CV for demo use: " + fileName).getBytes(StandardCharsets.UTF_8));
        }
    }

    private TAProfile createProfile(String id, String fullName, String studentNumber, String email,
                                    String phone, String skills, String availability, String cvPath) {
        TAProfile profile = new TAProfile();
        profile.setId(id);
        profile.setFullName(fullName);
        profile.setStudentNumber(studentNumber);
        profile.setEmail(email);
        profile.setPhone(phone);
        profile.setSkills(skills);
        profile.setAvailability(availability);
        profile.setCvPath(cvPath);
        return profile;
    }

    private JobPosting createJob(String id, String moId, String moduleCode, String title, String skills,
                                 String requirements, String workload, String deadline, String status,
                                 String description) {
        JobPosting job = new JobPosting();
        job.setId(id);
        job.setMoId(moId);
        job.setModuleCode(moduleCode);
        job.setTitle(title);
        job.setSkills(skills);
        job.setRequirements(requirements);
        job.setWorkload(workload);
        job.setDeadline(deadline);
        job.setStatus(status);
        job.setDescription(description);
        return job;
    }

    private ApplicationRecord createApplication(String id, String jobId, String taId, String priority,
                                                String status, String notes, String submittedAt) {
        ApplicationRecord application = new ApplicationRecord();
        application.setId(id);
        application.setJobId(jobId);
        application.setTaId(taId);
        application.setPriority(priority);
        application.setStatus(status);
        application.setNotes(notes);
        application.setSubmittedAt(submittedAt);
        return application;
    }
}
