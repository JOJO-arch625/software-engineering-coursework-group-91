package com.group91.tars.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.group91.tars.model.ApplicationRecord;
import com.group91.tars.model.JobPosting;
import com.group91.tars.model.Notification;
import com.group91.tars.model.TAProfile;
import com.group91.tars.model.UserAccount;

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
    private static final Type ACCOUNT_LIST_TYPE = new TypeToken<List<UserAccount>>() { }.getType();
    private static final Type NOTIFICATION_LIST_TYPE = new TypeToken<List<Notification>>() { }.getType();

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Path rootDirectory = Paths.get(System.getProperty("user.dir"));
    private final Path dataDirectory = rootDirectory.resolve("data");
    private final Path uploadDirectory = rootDirectory.resolve(Paths.get("uploads", "cv"));
    private final Path profilesFile = dataDirectory.resolve("ta-profiles.json");
    private final Path jobsFile = dataDirectory.resolve("job-postings.json");
    private final Path applicationsFile = dataDirectory.resolve("applications.json");
    private final Path accountsFile = dataDirectory.resolve("accounts.json");
    private final Path notificationsFile = dataDirectory.resolve("notifications.json");

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
            ensureSeedAccounts();
            ensureSeedNotifications();
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

    public List<UserAccount> loadAccounts() {
        return readList(accountsFile, ACCOUNT_LIST_TYPE);
    }

    public void saveAccounts(List<UserAccount> accounts) {
        writeList(accountsFile, accounts);
    }

    public List<Notification> loadNotifications() {
        return readList(notificationsFile, NOTIFICATION_LIST_TYPE);
    }

    public void saveNotifications(List<Notification> notifications) {
        writeList(notificationsFile, notifications);
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

    private void ensureSeedAccounts() throws IOException {
        if (Files.exists(accountsFile) && Files.size(accountsFile) > 0) {
            return;
        }

        List<UserAccount> accounts = new ArrayList<UserAccount>();
        accounts.add(createAccount("acc-ta-1", "ta.demo", "TaDemo123", "Yuyanchen Long", "TA", "ta-1"));
        accounts.add(createAccount("acc-mo-1", "mo.demo", "MoDemo123", "Ling Ma", "MO", "mo-1"));
        accounts.add(createAccount("acc-admin-1", "admin.demo", "AdminDemo123", "Programme Admin", "ADMIN", "admin-1"));
        writeList(accountsFile, accounts);
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

    private UserAccount createAccount(String id, String username, String password,
                                      String displayName, String role, String linkedId) {
        UserAccount account = new UserAccount();
        account.setId(id);
        account.setUsername(username);
        account.setPassword(password);
        account.setDisplayName(displayName);
        account.setRole(role);
        account.setLinkedId(linkedId);
        return account;
    }

    private void ensureSeedNotifications() throws IOException {
        if (Files.exists(notificationsFile) && Files.size(notificationsFile) > 0) {
            return;
        }

        List<Notification> notifications = new ArrayList<Notification>();
        notifications.add(createNotification("notif-1", "ta-1", "status", "Your application for Digital Systems TA is now Under Review.", "/ta/applications", false, "2026-03-24 10:05"));
        notifications.add(createNotification("notif-2", "ta-1", "status", "Your application for Data Analytics TA has been Rejected.", "/ta/applications", false, "2026-03-23 19:00"));
        notifications.add(createNotification("notif-3", "ta-2", "status", "Your application for Object-Oriented Programming TA has been Accepted.", "/ta/applications", true, "2026-03-22 09:30"));
        notifications.add(createNotification("notif-4", "ta-3", "status", "Your application for Digital Systems TA has been Accepted.", "/ta/applications", true, "2026-03-22 09:40"));
        notifications.add(createNotification("notif-5", "ta-3", "status", "Your application for Object-Oriented Programming TA has been Accepted.", "/ta/applications", true, "2026-03-23 14:50"));
        notifications.add(createNotification("notif-6", "ta-3", "overload", "You have reached the workload cap of 3 accepted jobs.", "/ta/applications", false, "2026-03-23 15:00"));
        notifications.add(createNotification("notif-7", "mo-1", "review", "New applicant for Digital Systems TA: Yuyanchen Long.", "/mo/review?jobId=job-2", false, "2026-03-24 10:00"));
        notifications.add(createNotification("notif-8", "mo-1", "review", "New applicant for Object-Oriented Programming TA: Ming Li.", "/mo/review?jobId=job-1", true, "2026-03-22 09:00"));
        notifications.add(createNotification("notif-9", "mo-1", "deadline", "Data Analytics TA posting deadline has passed.", "/mo/jobs/edit?id=job-3", true, "2026-04-09 23:59"));
        notifications.add(createNotification("notif-10", "admin-1", "overload", "Siyu Chen has reached the workload cap of 3 accepted jobs.", "/admin/workload", false, "2026-03-23 15:00"));
        writeList(notificationsFile, notifications);
    }

    private Notification createNotification(String id, String userId, String category, String message, String link, boolean read, String createdAt) {
        Notification notification = new Notification();
        notification.setId(id);
        notification.setUserId(userId);
        notification.setCategory(category);
        notification.setMessage(message);
        notification.setLink(link);
        notification.setRead(read);
        notification.setCreatedAt(createdAt);
        return notification;
    }
}
