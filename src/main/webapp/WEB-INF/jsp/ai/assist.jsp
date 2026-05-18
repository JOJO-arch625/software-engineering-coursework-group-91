<%@ page import="java.util.List,com.group91.tars.model.ApplicationRecord,com.group91.tars.model.JobPosting,com.group91.tars.model.TAProfile,com.group91.tars.model.ai.AiWorkloadAdvice,com.group91.tars.service.TarsService" %>
<%
    List<String> aiTodos = (List<String>) request.getAttribute("aiTodos");
    TarsService pageService = TarsService.getInstance();
%>
<%@ include file="../fragments/pageStart.jspf" %>
<%
    TAProfile currentTaProfile = null;
    boolean hasPdfCv = false;
    if ("TA".equals(currentRole) && currentUser != null) {
        currentTaProfile = pageService.getTaProfile(currentUser.getLinkedId());
        String cvPath = currentTaProfile == null ? null : currentTaProfile.getCvPath();
        hasPdfCv = cvPath != null && cvPath.toLowerCase().endsWith(".pdf");
    }
    String chatPrompt = "Ask about TA fit, job applicants, CV evidence, or workload risk.";
    String chatPlaceholder = "Analyze ta-1 for job-2 and explain missing skills plus workload risk.";
    String roleToolTitle = currentRole == null ? "Role-Specific Tools" : currentRole + " Workspace";
    if ("TA".equals(currentRole)) {
        chatPrompt = "Ask which open jobs fit your profile, what skills you are missing, whether your CV gives enough evidence, or how many more roles you can apply for.";
        chatPlaceholder = "Which open jobs fit my profile best?";
        roleToolTitle = "TA Application Assistant";
    } else if ("MO".equals(currentRole)) {
        chatPrompt = "Ask about applicants for your jobs, candidate summaries, CV evidence, shortlist advice, or applicant workload risk.";
        chatPlaceholder = "Summarize applicants for job-2 and give shortlist advice.";
        roleToolTitle = "MO Hiring Assistant";
    } else if ("ADMIN".equals(currentRole)) {
        chatPrompt = "Ask about aggregate TA workload, staffing risk, overloaded TAs, or system-level recruitment signals.";
        chatPlaceholder = "Which TAs have workload risk across the system?";
        roleToolTitle = "Admin Oversight Assistant";
    }
%>
<section class="view active ai-workbench" data-agent-workspace>
    <article class="ai-workbench-hero">
        <div>
            <p class="eyebrow">AI Agent Workspace</p>
            <h3>TA Recruitment AI Agent</h3>
            <p class="muted">Use PDF CV evidence, tool-calling chat, and role-specific recruitment insights in one workspace.</p>
            <ul class="ai-capability-list">
                <% if ("TA".equals(currentRole)) { %>
                <li>Find open jobs that fit your own TA profile</li>
                <li>Check missing skills and CV evidence before applying</li>
                <li>Review application capacity and workload risk</li>
                <% } else if ("MO".equals(currentRole)) { %>
                <li>Summarize applicants for your own job postings</li>
                <li>Review candidate CV evidence and workload risk</li>
                <li>Generate advisory shortlist reasoning</li>
                <% } else if ("ADMIN".equals(currentRole)) { %>
                <li>Review aggregate TA workload signals</li>
                <li>Inspect staffing risk without changing applications</li>
                <li>Keep oversight evidence separated from CV text</li>
                <% } else { %>
                <li>Load TA CV evidence for fit analysis</li>
                <li>Ask natural-language questions through tool calling</li>
                <li>Review skills, missing evidence, and workload risk</li>
                <% } %>
                <li>Keep all hiring decisions human-controlled</li>
            </ul>
        </div>
        <nav class="ai-workbench-tabs" aria-label="AI workspace sections" data-workbench-tabs>
            <button type="button" data-tab-target="cv-module">CV Evidence</button>
            <button class="active" type="button" data-tab-target="chat-module">Agent Chat</button>
            <button type="button" data-tab-target="role-module"><%= currentRole == null ? "Role Tools" : currentRole + " Tools" %></button>
        </nav>
    </article>

    <div class="ai-workbench-page" id="cv-module" data-workbench-page>
    <div class="ai-stage-grid">
        <article class="panel ai-stage-panel">
            <div class="panel-header">
                <p class="eyebrow">Step 1</p>
                <h4>CV Evidence Source</h4>
            </div>
            <% if ("TA".equals(currentRole)) { %>
            <div class="ai-status-strip">
                <span class="ai-source-chip <%= hasPdfCv ? "ai-source-llm" : "ai-source-local" %>">
                    <%= hasPdfCv ? "PDF CV ready" : "PDF CV required for evidence" %>
                </span>
                <span class="pill pill-neutral"><%= currentTaProfile == null || currentTaProfile.getCvPath() == null ? "No CV linked" : currentTaProfile.getCvPath() %></span>
            </div>
            <form method="post" action="<%= request.getContextPath() %>/ta/profile" enctype="multipart/form-data" class="ai-upload-form">
                <input type="hidden" name="action" value="uploadCv">
                <input type="hidden" name="returnTo" value="/ai/assist">
                <label>
                    Upload PDF CV
                    <input type="file" name="cvFile" accept=".pdf,.doc,.docx" required>
                </label>
                <button class="primary-button" type="submit">Upload CV</button>
            </form>
            <p class="muted">PDF files can be parsed for CV evidence. DOC/DOCX uploads remain stored but are not analysed in v1.</p>
            <% } else if ("MO".equals(currentRole)) { %>
            <div class="ai-evidence-box">
                <strong>Applicant CV evidence</strong>
                <p>MO users access CV evidence through selected applicants. Use the chat area to ask for candidate summaries or shortlist analysis for your jobs.</p>
            </div>
            <a class="secondary-button" href="<%= request.getContextPath() %>/mo/review">Open Applicant Review</a>
            <% } else if ("ADMIN".equals(currentRole)) { %>
            <div class="ai-evidence-box">
                <strong>Read-only admin view</strong>
                <p>Admins do not upload CVs here. Use this workspace to inspect workload risk and ask aggregate questions without modifying JSON data.</p>
            </div>
            <a class="secondary-button" href="<%= request.getContextPath() %>/admin/workload">Open Workload Dashboard</a>
            <% } else { %>
            <div class="alert info">Log in to use CV evidence tools.</div>
            <% } %>
        </article>

        <article class="panel ai-stage-panel">
            <div class="panel-header">
                <p class="eyebrow">Agent status</p>
                <h4>Runtime Mode</h4>
            </div>
            <div class="ai-evidence-box">
                <strong>Tool-calling readiness</strong>
                <p>Set <code>AI_TOOL_CALLING_ENABLED=true</code>, <code>AI_MODE=llm</code>, and <code>LLM_API_KEY</code> to enable the multi-step tool loop.</p>
            </div>
            <ul class="feature-list">
                <% for (String item : aiTodos) { %>
                <li><%= item %></li>
                <% } %>
            </ul>
        </article>
    </div>
    </div>

    <div class="ai-workbench-page active" id="chat-module" data-workbench-page>
    <div class="agent-workspace">
        <article class="panel agent-chat-panel">
            <div class="panel-header">
                <p class="eyebrow">Step 2</p>
                <h4>Agent Chat</h4>
                <span class="ai-source-chip ai-source-llm">Tool calling</span>
            </div>
            <div class="agent-messages" data-agent-messages>
                <div class="agent-message agent-message-assistant">
                    <%= chatPrompt %>
                </div>
            </div>
            <form class="agent-chat-form" data-agent-form>
                <textarea name="message" rows="4" placeholder="<%= chatPlaceholder %>" data-agent-initial="<%= chatPrompt %>" required></textarea>
                <div class="button-row">
                    <button class="primary-button" type="submit">Send</button>
                    <button class="secondary-button" type="button" data-agent-clear>Clear Chat</button>
                </div>
            </form>
            <p class="ai-disclaimer">AI guidance is advisory only. The agent cannot accept, reject, or modify applications.</p>
        </article>

        <article class="panel agent-trace-panel">
            <div class="panel-header">
                <p class="eyebrow">Trace</p>
                <h4>Tool Calls</h4>
                <span class="ai-source-chip ai-source-local" data-agent-source>idle</span>
            </div>
            <div class="agent-trace-list" data-agent-trace>
                <div class="ai-evidence-box">No tool calls yet.</div>
            </div>
            <div class="ai-evidence-box agent-final-json">
                <strong>Final JSON</strong>
                <pre data-agent-final>{}</pre>
            </div>
        </article>
    </div>
    </div>

    <div class="ai-workbench-page" id="role-module" data-workbench-page>
    <article class="panel ai-role-panel">
        <div class="panel-header">
            <p class="eyebrow">Step 3</p>
            <h4><%= roleToolTitle %></h4>
        </div>
        <% if ("TA".equals(currentRole)) {
            List<JobPosting> openJobs = pageService.getOpenJobs();
        %>
        <div class="ai-role-grid">
            <div class="ai-evidence-box">
                <strong>My profile skills</strong>
                <p><%= currentTaProfile == null ? "Profile not set." : currentTaProfile.getSkills() %></p>
            </div>
            <% for (JobPosting job : openJobs) {
                int score = pageService.calculateFitScore(currentUser.getLinkedId(), job.getId());
                List<String> missing = pageService.getMissingSkills(currentUser.getLinkedId(), job.getId());
            %>
            <div class="ai-evidence-box">
                <strong><%= job.getModuleCode() %> - <%= job.getTitle() %></strong>
                <p>Fit score: <%= score %>%</p>
                <p>Missing skills: <%= missing.isEmpty() ? "None" : String.join(", ", missing) %></p>
                <a class="table-link" href="<%= request.getContextPath() %>/ta/job?id=<%= job.getId() %>">Open job detail</a>
            </div>
            <% } %>
        </div>
        <% } else if ("MO".equals(currentRole)) {
            List<JobPosting> moJobs = pageService.getJobsForMo(currentUser.getLinkedId());
        %>
        <div class="table-shell">
            <table>
                <thead>
                <tr>
                    <th>Job</th>
                    <th>Applicants</th>
                    <th>Suggested prompt</th>
                </tr>
                </thead>
                <tbody>
                <% for (JobPosting job : moJobs) {
                    List<ApplicationRecord> apps = pageService.getApplicationsForJob(job.getId());
                %>
                <tr>
                    <td><%= job.getModuleCode() %> - <%= job.getTitle() %></td>
                    <td><%= apps.size() %></td>
                    <td>Summarize applicants for <%= job.getId() %> and provide shortlist advice.</td>
                </tr>
                <% } %>
                </tbody>
            </table>
        </div>
        <% } else if ("ADMIN".equals(currentRole)) {
            List<AiWorkloadAdvice> workloadAdvice = pageService.getAiWorkloadAdvice();
        %>
        <div class="ai-role-grid">
            <% for (AiWorkloadAdvice item : workloadAdvice) { %>
            <div class="ai-evidence-box">
                <strong><%= item.getTaName() %></strong>
                <p>Accepted jobs: <%= item.getAcceptedCount() %> / 3</p>
                <p>Risk: <%= item.getWorkloadRisk() %></p>
                <p><%= item.getAdvice() %></p>
            </div>
            <% } %>
        </div>
        <% } else { %>
        <div class="alert info">Log in to see role-specific AI tools.</div>
        <% } %>
    </article>
    </div>
</section>
<script>
    (function () {
        var workspace = document.querySelector("[data-agent-workspace]");
        if (!workspace) {
            return;
        }
        var form = workspace.querySelector("[data-agent-form]");
        var textarea = form.querySelector("textarea");
        var initialPrompt = textarea.getAttribute("data-agent-initial") || "Ask about TA fit, job applicants, CV evidence, or workload risk.";
        var messages = workspace.querySelector("[data-agent-messages]");
        var trace = workspace.querySelector("[data-agent-trace]");
        var source = workspace.querySelector("[data-agent-source]");
        var finalJson = workspace.querySelector("[data-agent-final]");
        var endpoint = "<%= request.getContextPath() %>/ai/assist/chat";
        var clearEndpoint = "<%= request.getContextPath() %>/ai/assist/chat/clear";
        var clearButton = workspace.querySelector("[data-agent-clear]");
        var tabs = document.querySelectorAll("[data-tab-target]");
        var pages = document.querySelectorAll("[data-workbench-page]");

        tabs.forEach(function (tab) {
            tab.addEventListener("click", function () {
                var target = tab.getAttribute("data-tab-target");
                tabs.forEach(function (item) {
                    item.classList.toggle("active", item === tab);
                });
                pages.forEach(function (page) {
                    page.classList.toggle("active", page.id === target);
                });
            });
        });

        function addMessage(text, className) {
            var item = document.createElement("div");
            item.className = "agent-message " + className;
            item.textContent = text;
            messages.appendChild(item);
            messages.scrollTop = messages.scrollHeight;
        }

        function renderTrace(items) {
            trace.innerHTML = "";
            if (!items || !items.length) {
                var empty = document.createElement("div");
                empty.className = "ai-evidence-box";
                empty.textContent = "No tool calls returned.";
                trace.appendChild(empty);
                return;
            }
            items.forEach(function (item) {
                var box = document.createElement("div");
                box.className = "ai-evidence-box";
                var title = document.createElement("strong");
                title.textContent = item.tool + " - " + (item.success ? "success" : "failed");
                var detail = document.createElement("pre");
                detail.textContent = JSON.stringify(item, null, 2);
                box.appendChild(title);
                box.appendChild(detail);
                trace.appendChild(box);
            });
        }

        function resolveAgentReply(data) {
            var finalJson = data && data.finalJson ? data.finalJson : {};
            if (data && data.reply) {
                return data.reply;
            }
            if (finalJson.analysis) {
                return finalJson.analysis;
            }
            if (finalJson.advice) {
                return finalJson.advice;
            }
            if (finalJson.summary) {
                return finalJson.summary;
            }
            if (finalJson.workloadMessage) {
                return finalJson.workloadMessage;
            }
            return "Agent completed.";
        }

        function resetChatUi() {
            messages.innerHTML = "";
            addMessage(initialPrompt, "agent-message-assistant");
            source.textContent = "idle";
            source.className = "ai-source-chip ai-source-local";
            finalJson.textContent = "{}";
            renderTrace([]);
        }

        clearButton.addEventListener("click", function () {
            fetch(clearEndpoint, {
                method: "POST"
            }).then(function (response) {
                if (!response.ok) {
                    throw new Error("HTTP " + response.status);
                }
                resetChatUi();
            }).catch(function (error) {
                addMessage("Clear chat failed: " + error.message, "agent-message-assistant");
            });
        });

        form.addEventListener("submit", function (event) {
            event.preventDefault();
            var text = textarea.value.trim();
            if (!text) {
                return;
            }
            addMessage(text, "agent-message-user");
            textarea.value = "";
            source.textContent = "running";
            finalJson.textContent = "{}";
            renderTrace([]);

            fetch(endpoint, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({ message: text })
            }).then(function (response) {
                return response.text().then(function (body) {
                    try {
                        return JSON.parse(body);
                    } catch (parseError) {
                        throw new Error("Expected JSON from " + endpoint + " but received HTTP "
                            + response.status + ": " + body.substring(0, 160));
                    }
                });
            }).then(function (data) {
                source.textContent = data.sourceMode || "unknown";
                source.className = "ai-source-chip ai-source-" + (data.sourceMode === "error" ? "error" : "llm");
                addMessage(data.success ? resolveAgentReply(data) : (data.errorMessage || "Agent failed."), "agent-message-assistant");
                renderTrace(data.toolTrace || []);
                finalJson.textContent = JSON.stringify(data.finalJson || {}, null, 2);
            }).catch(function (error) {
                source.textContent = "error";
                source.className = "ai-source-chip ai-source-error";
                addMessage("Chat request failed: " + error.message, "agent-message-assistant");
            });
        });
    })();
</script>
<%@ include file="../fragments/pageEnd.jspf" %>
