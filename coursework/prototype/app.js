const state = {
  currentView: "gateway",
  selectedJobId: "job-1",
  selectedApplicantId: "app-1",
  taProfile: {
    taId: "231224653",
    name: "Yuyanchen Long",
    skills: "Java, Python, VHDL, Object-Oriented Programming",
  },
  profileComplete: true,
  cvFileName: "YuyanchenLong_CV.pdf",
  jobs: [
    {
      id: "job-1",
      moduleCode: "EIE3320",
      title: "Object-Oriented Programming TA",
      skills: "Java, OOP, debugging",
      deadline: "2026-03-26",
      moName: "Dr Alice Morgan",
      weeklyHours: 6,
      workload: "6 hours / week",
      description: "Support weekly labs and guide students through Java exercises.",
      requirements:
        "Strong Java syntax, classes, arrays, exception handling, and lab support communication.",
      status: "Open",
      applicants: 3,
    },
    {
      id: "job-2",
      moduleCode: "EIE2105",
      title: "Digital Systems TA",
      skills: "VHDL, Boolean logic, simulation",
      deadline: "2026-03-20",
      moName: "Dr Wei Zhang",
      weeklyHours: 5,
      workload: "5 hours / week",
      description: "Assist digital systems lab sessions and hardware simulation support.",
      requirements:
        "Basic circuit design, VHDL syntax, waveform debugging, and hardware fundamentals.",
      status: "Open",
      applicants: 2,
    },
    {
      id: "job-3",
      moduleCode: "ECS5001",
      title: "Data Analytics TA",
      skills: "Python, pandas, plotting",
      deadline: "2026-04-02",
      moName: "Dr Priya Shah",
      weeklyHours: 4,
      workload: "4 hours / week",
      description: "Help students with Python notebooks and analytics exercises.",
      requirements:
        "Python basics, data processing, notebook workflows, and problem explanation skills.",
      status: "Closed",
      applicants: 4,
    },
  ],
  applications: [
    {
      id: "app-1",
      jobId: "job-2",
      moduleCode: "EIE2105",
      title: "Digital Systems TA",
      priority: 1,
      status: "Under Review",
      notes: "I have VHDL lab experience and can support simulation debugging.",
      reviewerNotes: "MO reviewing VHDL experience.",
      skills: "VHDL, simulation, digital logic",
      acceptedJobs: 2,
      cvPath: "uploads/cv/YuyanchenLong_CV.pdf",
    },
    {
      id: "app-2",
      jobId: "job-3",
      moduleCode: "ECS5001",
      title: "Data Analytics TA",
      priority: 2,
      status: "Rejected",
      notes: "I have completed data analytics coursework and enjoy notebook support.",
      reviewerNotes: "Role filled. Transparent rejection shown to TA.",
      skills: "Python, pandas, plots",
      acceptedJobs: 2,
      cvPath: "uploads/cv/YuyanchenLong_CV.pdf",
    },
  ],
  notifications: [
    "Your Digital Systems application is under review.",
    "Data Analytics role status updated to Rejected.",
    "You can still apply for 1 more job.",
  ],
  adminRecords: [
    {
      taId: "231224653",
      ta: "Yuyanchen Long",
      assignments: [
        { moduleCode: "COMP5002", weeklyHours: 4 },
        { moduleCode: "EIE3001", weeklyHours: 6 },
      ],
      modules: ["COMP5002", "EIE3001"],
      count: 2,
    },
    {
      taId: "231209888",
      ta: "Ming Li",
      assignments: [{ moduleCode: "ECS4007", weeklyHours: 4 }],
      modules: ["ECS4007"],
      count: 1,
    },
    {
      taId: "231236666",
      ta: "Siyu Chen",
      assignments: [
        { moduleCode: "ECS5001", weeklyHours: 4 },
        { moduleCode: "EIE2105", weeklyHours: 5 },
        { moduleCode: "EIE3320", weeklyHours: 6 },
      ],
      modules: ["ECS5001", "EIE2105", "EIE3320"],
      count: 3,
    },
  ],
  adminSearchQuery: "",
};

const VALID_APPLICATION_STATUSES = [
  "Submitted",
  "Under Review",
  "Shortlisted",
  "Accepted",
  "Rejected",
];

const viewMeta = {
  gateway: { tag: "Gateway", title: "Prototype Overview" },
  "ta-dashboard": { tag: "TA Flow", title: "TA Dashboard" },
  "ta-profile": { tag: "TA Flow", title: "TA Profile And CV" },
  "job-list": { tag: "TA Flow", title: "Browse Job Postings" },
  "job-detail": { tag: "TA Flow", title: "Job Detail And Application" },
  applications: { tag: "TA Flow", title: "My Applications" },
  "mo-dashboard": { tag: "MO Flow", title: "MO Dashboard" },
  "job-editor": { tag: "MO Flow", title: "Create Or Edit Job Posting" },
  review: { tag: "MO Flow", title: "Applicant Review" },
  "admin-dashboard": { tag: "Admin Flow", title: "Admin Workload Dashboard" },
  "ai-assist": { tag: "Optional", title: "AI Assist Concept" },
};

function getOpenJobs() {
  return state.jobs.filter((job) => job.status === "Open");
}

function getAllJobs() {
  return [...state.jobs].sort((a, b) => new Date(a.deadline) - new Date(b.deadline));
}

function isValidApplicationStatus(status) {
  return VALID_APPLICATION_STATUSES.includes(status);
}

function getAcceptedApplications() {
  return state.applications.filter((app) => app.status === "Accepted");
}

function getSelectedJobApplications() {
  return state.applications.filter((app) => app.jobId === state.selectedJobId);
}

function getApplicantName(app) {
  return app?.applicantName || "Yuyanchen Long";
}

function getAdminRecordByApplicant(applicantName) {
  return state.adminRecords.find((item) => item.ta === applicantName);
}

function ensureAdminRecord(applicantName) {
  const existingRecord = getAdminRecordByApplicant(applicantName);
  if (existingRecord) return existingRecord;

  const record = {
    taId: "Pending",
    ta: applicantName,
    assignments: [],
    modules: [],
    count: 0,
  };
  state.adminRecords.push(record);
  return record;
}

function getWeeklyHoursForJob(moduleCode) {
  const job = state.jobs.find((item) => item.moduleCode === moduleCode);
  return job?.weeklyHours || 0;
}

function getTotalWeeklyHours(record) {
  if (record.assignments?.length) {
    return record.assignments.reduce((sum, item) => sum + Number(item.weeklyHours || 0), 0);
  }
  return record.modules.reduce((sum, moduleCode) => sum + getWeeklyHoursForJob(moduleCode), 0);
}

function getAcceptedJobCountForApplicant(app) {
  const record = getAdminRecordByApplicant(getApplicantName(app));
  return record ? record.count : app.acceptedJobs;
}

function syncAcceptedJobCountForApplicant(applicantName, acceptedCount) {
  state.applications.forEach((item) => {
    if (getApplicantName(item) === applicantName) {
      item.acceptedJobs = acceptedCount;
    }
  });
}

function removeAcceptedModuleFromRecord(app) {
  const record = getAdminRecordByApplicant(getApplicantName(app));
  if (!record) return;

  record.modules = record.modules.filter((moduleCode) => moduleCode !== app.moduleCode);
  record.assignments = (record.assignments || []).filter(
    (assignment) => assignment.moduleCode !== app.moduleCode
  );
  record.count = record.modules.length;
  syncAcceptedJobCountForApplicant(record.ta, record.count);
}

function addAcceptedModuleToRecord(app) {
  const applicantName = getApplicantName(app);
  const record = ensureAdminRecord(applicantName);
  if (!record.modules.includes(app.moduleCode)) {
    record.modules.push(app.moduleCode);
  }
  if (!(record.assignments || []).some((assignment) => assignment.moduleCode === app.moduleCode)) {
    record.assignments = record.assignments || [];
    record.assignments.push({
      moduleCode: app.moduleCode,
      weeklyHours: getWeeklyHoursForJob(app.moduleCode),
    });
  }
  record.count = record.modules.length;
  syncAcceptedJobCountForApplicant(applicantName, record.count);
}

function normalizeSkillTokens(text) {
  return String(text || "")
    .toLowerCase()
    .replace(/object-oriented programming/g, "oop")
    .split(/[^a-z0-9+#]+/)
    .map((item) => item.trim())
    .filter(Boolean);
}

function getRecommendedJobsForTa(taProfile = state.taProfile, jobs = getAllJobs()) {
  const taSkills = new Set(normalizeSkillTokens(taProfile.skills));
  return jobs
    .filter((job) => job.status === "Open")
    .map((job) => {
      const requiredSkills = [...new Set(normalizeSkillTokens(job.skills))];
      const matchedSkills = requiredSkills.filter((skill) => taSkills.has(skill));
      const totalSkills = requiredSkills.length || 1;
      const matchRate = Math.round((matchedSkills.length / totalSkills) * 100);
      return {
        job,
        matchedSkills,
        totalSkills,
        matchRate,
        explanation: `Matched ${matchedSkills.length}/${totalSkills} required skills; match rate ${matchRate}%.`,
      };
    })
    .filter((item) => item.matchRate >= 60)
    .sort((a, b) => b.matchRate - a.matchRate || new Date(a.job.deadline) - new Date(b.job.deadline))
    .slice(0, 5);
}

function getFilteredAdminRecords() {
  const query = state.adminSearchQuery.trim().toLowerCase();
  if (!query) return state.adminRecords;
  return state.adminRecords.filter(
    (record) =>
      record.ta.toLowerCase().includes(query) ||
      String(record.taId || "").toLowerCase().includes(query)
  );
}

function setView(viewId) {
  if (!viewMeta[viewId]) return;

  state.currentView = viewId;
  document.querySelectorAll(".view").forEach((view) => {
    view.classList.toggle("active", view.id === viewId);
  });
  document.querySelectorAll(".nav-link").forEach((button) => {
    button.classList.toggle("active", button.dataset.view === viewId);
  });

  const meta = viewMeta[viewId];
  document.getElementById("view-tag").textContent = meta.tag;
  document.getElementById("view-title").textContent = meta.title;
}

function statusClass(status) {
  if (status === "Open" || status === "Submitted") return "status-open";
  if (status === "Under Review") return "status-review";
  if (status === "Shortlisted") return "status-shortlisted";
  if (status === "Accepted") return "status-accepted";
  return "status-rejected";
}

function createStatusChip(status) {
  return `<span class="status-chip ${statusClass(status)}">${status}</span>`;
}

function setReviewActionsDisabled(disabled) {
  ["mark-review-button", "shortlist-button", "bulk-shortlist-button", "accept-button", "reject-button"].forEach((id) => {
    const button = document.getElementById(id);
    if (button) button.disabled = disabled;
  });
}

function updateApplicationStatus(app, status, reviewerNotes) {
  if (!app || !isValidApplicationStatus(status)) return false;
  const wasAccepted = app.status === "Accepted";
  app.status = status;
  if (reviewerNotes !== undefined) {
    app.reviewerNotes = reviewerNotes;
  }
  if (wasAccepted && status !== "Accepted") {
    removeAcceptedModuleFromRecord(app);
  }
  return true;
}

function showReviewFeedback(type, message) {
  const feedback = document.getElementById("review-feedback");
  feedback.textContent = message;
  feedback.className = `alert ${type}`;
}

function renderEmptyReviewState(selectedJob) {
  document.getElementById("candidate-name").textContent = selectedJob
    ? `No applicants for ${selectedJob.moduleCode}`
    : "No applicant selected";
  document.getElementById("candidate-skills").textContent =
    "Applicants for this posting will appear here after a TA submits an application.";
  document.getElementById("candidate-cv").textContent = "CV path: Not available yet";
  document.getElementById("candidate-workload").textContent = "Current accepted jobs: - / 3";
  document.getElementById("candidate-motivation").textContent = "No motivation note available.";
  document.getElementById("candidate-reviewer-notes").value = "";
  document.getElementById("review-ai-score").textContent = "--";
  document.getElementById("review-ai-fit").textContent =
    "No applicant is currently selected for AI review support.";
  document.getElementById("review-ai-workload").textContent =
    "Workload analysis will appear once an applicant is available.";
  document.getElementById("review-ai-action").textContent =
    "Open a posting with applicants to review recommendation details.";
  setReviewActionsDisabled(true);
}

function renderTopMetrics() {
  document.getElementById("open-job-count").textContent = getOpenJobs().length;
  document.getElementById("application-count").textContent = state.applications.length;
  document.getElementById("accepted-count").textContent = getAcceptedApplications().length;

  document.getElementById("profile-status").textContent = state.profileComplete
    ? "Complete"
    : "Incomplete";
  document.getElementById("ta-application-usage").textContent = `${state.applications.length} / 3`;
  document.getElementById("ta-accepted-usage").textContent = `${getAcceptedApplications().length} / 3`;
}

function renderNotifications() {
  document.getElementById("ta-notification-list").innerHTML = state.notifications
    .map((item) => `<li>${item}</li>`)
    .join("");
}

function renderRecommendations() {
  const list = document.getElementById("ta-recommendation-list");
  if (!list) return;
  const recommendations = getRecommendedJobsForTa();
  list.innerHTML = recommendations.length
    ? recommendations
        .map(
          ({ job, matchedSkills, totalSkills, matchRate }) => `
            <article class="recommendation-item">
              <div>
                <p class="eyebrow">${job.moduleCode}</p>
                <h5>${job.title}</h5>
                <p class="muted">MO: ${job.moName} - Deadline: ${job.deadline}</p>
              </div>
              <strong>${matchRate}%</strong>
              <p>Matched ${matchedSkills.length}/${totalSkills} required skills: ${
                matchedSkills.length ? matchedSkills.join(", ") : "none"
              }.</p>
              <p class="muted">AI recommendation is for reference only. Final application choice remains with the TA.</p>
            </article>
          `
        )
        .join("")
    : '<p class="muted">No open job currently reaches the 60% skill-match threshold.</p>';
}

function renderJobs() {
  const jobGrid = document.getElementById("job-list-grid");
  jobGrid.innerHTML = getAllJobs()
    .map(
      (job) => `
        <article class="job-card">
          <header>
            <div>
              <p class="eyebrow">${job.moduleCode}</p>
              <h5>${job.title}</h5>
            </div>
            ${createStatusChip(job.status)}
          </header>
          <p class="muted">${job.description}</p>
          <div class="job-meta">
            <span class="pill pill-neutral">${job.skills}</span>
            <span class="pill pill-neutral">${job.workload}</span>
            <span class="pill pill-neutral">Deadline: ${job.deadline}</span>
            <span class="pill pill-neutral">MO: ${job.moName}</span>
          </div>
          <div class="button-row">
            <button class="secondary-button" data-select-job="${job.id}">View detail</button>
          </div>
        </article>
      `
    )
    .join("");
}

function renderJobDetail() {
  const job = state.jobs.find((item) => item.id === state.selectedJobId);
  if (!job) return;
  document.getElementById("detail-module-code").textContent = job.moduleCode;
  document.getElementById("detail-title").textContent = job.title;
  document.getElementById("detail-skills").textContent = job.skills;
  document.getElementById("detail-workload").textContent = job.workload;
  document.getElementById("detail-deadline").textContent = job.deadline;
  document.getElementById("detail-mo").textContent = job.moName;
  document.getElementById("detail-description").textContent = job.description;
  document.getElementById("detail-requirements").textContent = job.requirements;
  document.getElementById("detail-status").textContent = job.status;
  document.getElementById("detail-status").className =
    `pill ${job.status === "Open" ? "pill-success" : "pill-warning"}`;
  renderJobAI(job);
}

function renderApplications() {
  document.getElementById("applications-table").innerHTML = state.applications
    .map(
      (app) => `
        <tr>
          <td>${app.moduleCode}<br /><span class="muted">${app.title}</span></td>
          <td>Priority ${app.priority}</td>
          <td>${createStatusChip(app.status)}</td>
          <td>
            <strong>Motivation:</strong> ${app.notes}<br />
            <span class="muted"><strong>MO review:</strong> ${app.reviewerNotes || "No reviewer note yet."}</span>
          </td>
        </tr>
      `
    )
    .join("");
}

function renderMODashboard() {
  document.getElementById("mo-open-count").textContent = getOpenJobs().length;
  document.getElementById("mo-applicant-count").textContent = state.jobs.reduce(
    (sum, job) => sum + job.applicants,
    0
  );
  document.getElementById("mo-pending-count").textContent = state.applications.filter(
    (app) => app.status === "Submitted" || app.status === "Under Review" || app.status === "Shortlisted"
  ).length;

  document.getElementById("mo-posting-list").innerHTML = state.jobs
    .map(
      (job) => `
        <article class="posting-card">
          <header>
            <div>
              <p class="eyebrow">${job.moduleCode}</p>
              <h5>${job.title}</h5>
            </div>
            ${createStatusChip(job.status)}
          </header>
          <p class="muted">${job.skills}</p>
          <p class="muted">Deadline: ${job.deadline} - Weekly hours: ${job.weeklyHours}</p>
          <p>${job.applicants} applicants</p>
          <div class="button-row">
            <button class="secondary-button" data-select-job="${job.id}">Open detail</button>
            <button class="ghost-button" data-review-job="${job.id}">Review applicants</button>
          </div>
        </article>
      `
    )
    .join("");
}

function renderReviewTable() {
  const selectedJob = state.jobs.find((item) => item.id === state.selectedJobId);
  const filteredApplications = getSelectedJobApplications();

  document.getElementById("review-job-context").textContent = selectedJob
    ? `Showing applicants for ${selectedJob.moduleCode} ${selectedJob.title}.`
    : "Showing applicants for the selected job posting.";

  if (filteredApplications.length === 0) {
    document.getElementById("review-table").innerHTML = `
      <tr>
        <td colspan="4" class="muted">No applicants have applied for this job yet.</td>
      </tr>
    `;
    hide("review-feedback");
    renderEmptyReviewState(selectedJob);
    return;
  }

  if (!filteredApplications.some((item) => item.id === state.selectedApplicantId)) {
    state.selectedApplicantId = filteredApplications[0].id;
  }

  document.getElementById("review-table").innerHTML = filteredApplications
    .map(
      (app) => `
        <tr>
          <td><button class="table-link" data-select-app="${app.id}">${getApplicantName(app)}</button></td>
          <td>${app.skills}</td>
          <td>${getAcceptedJobCountForApplicant(app)} / 3</td>
          <td>${createStatusChip(app.status)}</td>
        </tr>
      `
    )
    .join("");

  const selectedApp = filteredApplications.find((item) => item.id === state.selectedApplicantId);
  if (!selectedApp) {
    renderEmptyReviewState(selectedJob);
    return;
  }

  const acceptedJobs = getAcceptedJobCountForApplicant(selectedApp);
  document.getElementById("candidate-name").textContent = getApplicantName(selectedApp);
  document.getElementById("candidate-skills").textContent = selectedApp.skills;
  document.getElementById("candidate-cv").textContent = `CV path: ${selectedApp.cvPath}`;
  document.getElementById(
    "candidate-workload"
  ).textContent = `Current accepted jobs: ${acceptedJobs} / 3`;
  document.getElementById("candidate-motivation").textContent = selectedApp.notes;
  document.getElementById("candidate-reviewer-notes").value = selectedApp.reviewerNotes || "";
  setReviewActionsDisabled(false);
  renderReviewAI({ ...selectedApp, acceptedJobs });
}

function renderJobAI(job) {
  const aiByJob = {
    "job-1": {
      score: "88%",
      strengths: "Strong Java background and prior lab support.",
      gaps: "No critical gap detected for this role.",
      recommendation: "Good fit. Priority 1 or 2 is reasonable if workload remains below the cap.",
    },
    "job-2": {
      score: "76%",
      strengths: "Relevant VHDL and digital logic experience are present.",
      gaps: "Could strengthen hardware debugging examples in the profile.",
      recommendation: "Viable fit. A supporting note about simulation tools would help.",
    },
    "job-3": {
      score: "61%",
      strengths: "General Python familiarity matches the module baseline.",
      gaps: "Pandas and notebook workflow evidence is weaker than for other roles.",
      recommendation: "Apply only if higher-priority roles are unavailable.",
    },
    "job-4": {
      score: "84%",
      strengths: "Strong Java and teamwork background suit software engineering labs.",
      gaps: "Testing evidence could be described more explicitly.",
      recommendation: "Good match for lab support and agile coursework assistance.",
    },
  };

  const ai = aiByJob[job.id] || aiByJob["job-1"];
  document.getElementById("job-ai-score").textContent = ai.score;
  document.getElementById("job-ai-strengths").textContent = ai.strengths;
  document.getElementById("job-ai-gaps").textContent = ai.gaps;
  document.getElementById("job-ai-recommendation").textContent = ai.recommendation;
}

function renderReviewAI(app) {
  const workloadLevel =
    app.acceptedJobs >= 3 ? "High workload risk. Acceptance would exceed the workload cap." :
    app.acceptedJobs === 2 ? "Current accepted load is moderate. Review before final acceptance." :
    "Current accepted load is low and acceptable.";

  document.getElementById("review-ai-score").textContent =
    app.status === "Rejected" ? "58%" : app.status === "Accepted" ? "90%" : "82%";
  document.getElementById("review-ai-fit").textContent =
    `Skill alignment for ${app.moduleCode} is based on ${app.skills}.`;
  document.getElementById("review-ai-workload").textContent = workloadLevel;
  document.getElementById("review-ai-action").textContent =
    app.acceptedJobs >= 3
      ? "Reject or reassign due to workload overload."
      : app.acceptedJobs === 2
        ? "Mark under review if another high-priority TA has lighter workload."
        : "Suitable candidate if profile and CV checks are complete.";
}

function renderAdminDashboard() {
  const filteredRecords = getFilteredAdminRecords();
  const acceptedCount = state.adminRecords.reduce((sum, item) => sum + item.count, 0);
  const alertCount = state.adminRecords.filter((item) => getTotalWeeklyHours(item) > 10).length;

  document.getElementById("admin-ta-count").textContent = state.adminRecords.length;
  document.getElementById("admin-accepted-count").textContent = acceptedCount;
  document.getElementById("admin-alert-count").textContent = alertCount;
  const searchInput = document.getElementById("admin-search-input");
  if (searchInput && searchInput.value !== state.adminSearchQuery) {
    searchInput.value = state.adminSearchQuery;
  }
  document.getElementById("admin-overload-banner").textContent =
    alertCount > 0
      ? `${alertCount} TA${alertCount > 1 ? "s are" : " is"} above 10 weekly hours. Review allocation before confirming more offers.`
      : "No TA is currently above the 10 weekly-hour threshold.";
  document
    .getElementById("admin-overload-banner")
    .classList.toggle("danger", alertCount > 0);
  document
    .getElementById("admin-overload-banner")
    .classList.toggle("info", alertCount === 0);

  document.getElementById("admin-table").innerHTML = filteredRecords
    .map((record) => {
      const totalWeeklyHours = getTotalWeeklyHours(record);
      const badge =
        totalWeeklyHours > 10
          ? '<span class="status-chip status-overload">Over 10 hours</span>'
          : totalWeeklyHours >= 8
            ? '<span class="status-chip status-near">Near limit</span>'
            : '<span class="status-chip status-accepted">Balanced</span>';
      return `
        <tr class="${totalWeeklyHours > 10 ? "overload-row" : ""}">
          <td>${record.ta}<br /><span class="muted">${record.taId}</span></td>
          <td>${record.modules.join(", ")}</td>
          <td>${record.count}</td>
          <td>${totalWeeklyHours}</td>
          <td>${badge}</td>
        </tr>
      `;
    })
    .join("") || '<tr><td colspan="5" class="muted">No TA matches this search.</td></tr>';
}

function show(id) {
  document.getElementById(id).classList.remove("hidden");
}

function hide(id) {
  document.getElementById(id).classList.add("hidden");
}

function resetMessages() {
  [
    "profile-warning",
    "upload-success",
    "upload-error",
    "apply-success",
    "apply-limit",
    "apply-closed",
    "apply-case-note",
    "job-save-success",
    "job-close-info",
    "review-feedback",
  ].forEach(hide);
}

function addApplication(jobId, priority) {
  const job = state.jobs.find((item) => item.id === jobId);
  if (!job) return;
  const applicantName = "Yuyanchen Long";
  state.applications.push({
    id: `app-${Date.now()}`,
    jobId: job.id,
    moduleCode: job.moduleCode,
    title: job.title,
    priority,
    status: "Submitted",
    notes: document.getElementById("motivation-note")?.value || "Application submitted successfully.",
    reviewerNotes: "",
    skills: job.skills,
    acceptedJobs: getAdminRecordByApplicant(applicantName)?.count ?? getAcceptedApplications().length,
    cvPath: `uploads/cv/${state.cvFileName}`,
  });
  state.notifications.unshift(`New application submitted for ${job.moduleCode}.`);
  if (state.notifications.length > 4) state.notifications.pop();
  job.applicants += 1;
}

function rerender() {
  renderTopMetrics();
  renderNotifications();
  renderRecommendations();
  renderJobs();
  renderJobDetail();
  renderApplications();
  renderMODashboard();
  renderReviewTable();
  renderAdminDashboard();
}

function bindEvents() {
  document.querySelectorAll(".nav-link").forEach((button) => {
    button.addEventListener("click", () => setView(button.dataset.view));
  });

  document.addEventListener("click", (event) => {
    const reviewTrigger = event.target.closest("[data-review-job]");
    if (reviewTrigger) {
      state.selectedJobId = reviewTrigger.dataset.reviewJob;
      state.selectedApplicantId = getSelectedJobApplications()[0]?.id || "";
      hide("review-feedback");
      setView("review");
      renderReviewTable();
      return;
    }

    const jumpTrigger = event.target.closest("[data-jump]");
    if (jumpTrigger) {
      setView(jumpTrigger.dataset.jump);
      return;
    }

    const jobTrigger = event.target.closest("[data-select-job]");
    if (jobTrigger) {
      state.selectedJobId = jobTrigger.dataset.selectJob;
      renderJobDetail();
      setView("job-detail");
    }

    const appTrigger = event.target.closest("[data-select-app]");
    if (appTrigger) {
      state.selectedApplicantId = appTrigger.dataset.selectApp;
      hide("review-feedback");
      renderReviewTable();
    }
  });

  document.getElementById("toggle-profile-warning").addEventListener("click", () => {
    hide("upload-success");
    hide("upload-error");
    show("profile-warning");
  });

  document.getElementById("save-profile-button").addEventListener("click", () => {
    state.profileComplete = true;
    hide("profile-warning");
    renderTopMetrics();
  });

  document.getElementById("upload-valid-button").addEventListener("click", () => {
    state.cvFileName = "YuyanchenLong_CV.pdf";
    document.getElementById("cv-name").textContent = state.cvFileName;
    hide("upload-error");
    show("upload-success");
  });

  document.getElementById("upload-invalid-button").addEventListener("click", () => {
    hide("upload-success");
    show("upload-error");
  });

  document.getElementById("apply-button").addEventListener("click", () => {
    resetMessages();
    const job = state.jobs.find((item) => item.id === state.selectedJobId);
    if (job.status !== "Open") {
      show("apply-closed");
      return;
    }
    if (state.applications.length >= 3) {
      show("apply-limit");
      return;
    }
    addApplication(job.id, Number(document.getElementById("priority-select").value));
    show("apply-success");
    rerender();
  });

  document.getElementById("simulate-limit-button").addEventListener("click", () => {
    hide("apply-success");
    hide("apply-closed");
    hide("apply-case-note");
    show("apply-limit");
  });

  document.getElementById("select-closed-job-button").addEventListener("click", () => {
    state.selectedJobId = "job-3";
    renderJobDetail();
    hide("apply-success");
    hide("apply-limit");
    hide("apply-closed");
    show("apply-case-note");
  });

  document.getElementById("refresh-status-button").addEventListener("click", rerender);

  document.getElementById("save-job-button").addEventListener("click", () => {
    const existing = state.jobs.find((item) => item.id === "job-4");
    if (!existing) {
      state.jobs.unshift({
        id: "job-4",
        moduleCode: "ECS3010",
        title: "Software Engineering Lab TA",
        skills: "Java, teamwork, testing",
        deadline: "2026-03-24",
        moName: "Dr Emily Carter",
        weeklyHours: 5,
        workload: "5 hours / week",
        description: "Support agile labs, debugging sessions, and requirement clarification.",
        requirements:
          "Basic software engineering concepts, Java skills, teamwork, and issue tracking awareness.",
        status: "Open",
        applicants: 0,
      });
    }
    hide("job-close-info");
    show("job-save-success");
    rerender();
  });

  document.getElementById("close-job-button").addEventListener("click", () => {
    const job = state.jobs.find((item) => item.id === "job-1");
    job.status = "Closed";
    hide("job-save-success");
    show("job-close-info");
    rerender();
  });

  document.getElementById("mark-review-button").addEventListener("click", () => {
    const app = state.applications.find((item) => item.id === state.selectedApplicantId);
    if (!app) return;
    const reviewerNotes = document.getElementById("candidate-reviewer-notes").value;
    updateApplicationStatus(app, "Under Review", reviewerNotes || "MO is reviewing workload and module fit.");
    showReviewFeedback("success", `Applicant marked under review for ${app.moduleCode}.`);
    rerender();
  });

  document.getElementById("shortlist-button").addEventListener("click", () => {
    const app = state.applications.find((item) => item.id === state.selectedApplicantId);
    if (!app) return;
    const reviewerNotes = document.getElementById("candidate-reviewer-notes").value;
    updateApplicationStatus(app, "Shortlisted", reviewerNotes || "Shortlisted for final MO comparison.");
    state.notifications.unshift(`${app.moduleCode} status updated to Shortlisted.`);
    if (state.notifications.length > 4) state.notifications.pop();
    showReviewFeedback("success", `Applicant shortlisted for ${app.moduleCode}.`);
    rerender();
  });

  document.getElementById("bulk-shortlist-button").addEventListener("click", () => {
    const apps = getSelectedJobApplications();
    apps.forEach((app) => {
      if (app.status !== "Accepted" && app.status !== "Rejected") {
        updateApplicationStatus(app, "Shortlisted", "Bulk shortlisted by MO for final review.");
      }
    });
    state.notifications.unshift("All eligible applicants for this posting were marked Shortlisted.");
    if (state.notifications.length > 4) state.notifications.pop();
    showReviewFeedback("success", `${apps.length} applicant record(s) processed for shortlist.`);
    rerender();
  });

  document.getElementById("accept-button").addEventListener("click", () => {
    const app = state.applications.find((item) => item.id === state.selectedApplicantId);
    if (!app) return;

    const applicantName = getApplicantName(app);
    const acceptedJobCount = getAcceptedJobCountForApplicant(app);

    if (app.status === "Accepted") {
      showReviewFeedback("info", `${applicantName} has already been accepted for ${app.moduleCode}.`);
      return;
    }

    if (acceptedJobCount >= 3) {
      showReviewFeedback(
        "danger",
        `${applicantName} already has 3 accepted jobs. This application cannot be accepted.`
      );
      return;
    }

    updateApplicationStatus(
      app,
      "Accepted",
      document.getElementById("candidate-reviewer-notes").value || "Offer sent to TA and visible in dashboard."
    );
    addAcceptedModuleToRecord(app);

    state.notifications.unshift(`${app.moduleCode} status updated to Accepted.`);
    if (state.notifications.length > 4) state.notifications.pop();
    showReviewFeedback("success", `Applicant accepted for ${app.moduleCode}.`);
    rerender();
  });

  document.getElementById("reject-button").addEventListener("click", () => {
    const app = state.applications.find((item) => item.id === state.selectedApplicantId);
    if (!app) return;
    updateApplicationStatus(
      app,
      "Rejected",
      document.getElementById("candidate-reviewer-notes").value ||
        "MO rejected the application. TA can see the result clearly."
    );
    state.notifications.unshift(`${app.moduleCode} status updated to Rejected.`);
    if (state.notifications.length > 4) state.notifications.pop();
    showReviewFeedback("success", `Applicant rejected for ${app.moduleCode}.`);
    rerender();
  });

  document.getElementById("admin-search-input").addEventListener("input", (event) => {
    state.adminSearchQuery = event.target.value;
    renderAdminDashboard();
  });
}

function init() {
  if (typeof document !== "undefined") {
    setView(state.currentView);
    bindEvents();
    rerender();
  }
}

init();

if (typeof module !== "undefined") {
  module.exports = {
    state,
    VALID_APPLICATION_STATUSES,
    getAllJobs,
    getTotalWeeklyHours,
    getRecommendedJobsForTa,
    normalizeSkillTokens,
    isValidApplicationStatus,
    updateApplicationStatus,
  };
}
