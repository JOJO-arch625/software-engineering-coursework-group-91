<%@ page import="java.util.List,com.group91.tars.model.ApplicationRecord,com.group91.tars.model.JobPosting,com.group91.tars.model.TAProfile,com.group91.tars.model.ai.AiWorkloadAdvice,com.group91.tars.service.TarsService" %>
<%
    List<String> aiTodos = (List<String>) request.getAttribute("aiTodos");
    String aiChatMessagesJson = (String) request.getAttribute("aiChatMessagesJson");
    String aiChatTraceJson = (String) request.getAttribute("aiChatTraceJson");
    String aiChatFinalJson = (String) request.getAttribute("aiChatFinalJson");
    if (aiChatMessagesJson == null) aiChatMessagesJson = "[]";
    if (aiChatTraceJson == null) aiChatTraceJson = "[]";
    if (aiChatFinalJson == null) aiChatFinalJson = "{}";
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
    String chatPrompt = i18n.t("ai.assist.chat.prompt.default");
    String chatPlaceholder = i18n.t("ai.assist.chat.placeholder.default");
    String roleToolTitle = currentRole == null ? i18n.t("ai.assist.role-tools") : i18n.t("ai.assist.role-workspace", currentRole);
    if ("TA".equals(currentRole)) {
        chatPrompt = i18n.t("ai.assist.chat.prompt.ta");
        chatPlaceholder = i18n.t("ai.assist.chat.placeholder.ta");
        roleToolTitle = i18n.t("ai.assist.role-title.ta");
    } else if ("MO".equals(currentRole)) {
        chatPrompt = i18n.t("ai.assist.chat.prompt.mo");
        chatPlaceholder = i18n.t("ai.assist.chat.placeholder.mo");
        roleToolTitle = i18n.t("ai.assist.role-title.mo");
    } else if ("ADMIN".equals(currentRole)) {
        chatPrompt = i18n.t("ai.assist.chat.prompt.admin");
        chatPlaceholder = i18n.t("ai.assist.chat.placeholder.admin");
        roleToolTitle = i18n.t("ai.assist.role-title.admin");
    }
%>
<section class="view active ai-workbench" data-agent-workspace>
    <article class="ai-workbench-hero">
        <div>
            <p class="eyebrow"><%= i18n.t("ai.assist.agent.eyebrow") %></p>
            <h3><%= i18n.t("ai.assist.agent.heading") %></h3>
            <p class="muted"><%= i18n.t("ai.assist.agent.description") %></p>
            <ul class="ai-capability-list">
                <% if ("TA".equals(currentRole)) { %>
                <li><%= i18n.t("ai.assist.capability.ta-1") %></li>
                <li><%= i18n.t("ai.assist.capability.ta-2") %></li>
                <li><%= i18n.t("ai.assist.capability.ta-3") %></li>
                <% } else if ("MO".equals(currentRole)) { %>
                <li><%= i18n.t("ai.assist.capability.mo-1") %></li>
                <li><%= i18n.t("ai.assist.capability.mo-2") %></li>
                <li><%= i18n.t("ai.assist.capability.mo-3") %></li>
                <% } else if ("ADMIN".equals(currentRole)) { %>
                <li><%= i18n.t("ai.assist.capability.admin-1") %></li>
                <li><%= i18n.t("ai.assist.capability.admin-2") %></li>
                <li><%= i18n.t("ai.assist.capability.admin-3") %></li>
                <% } else { %>
                <li><%= i18n.t("ai.assist.capability.default-1") %></li>
                <li><%= i18n.t("ai.assist.capability.default-2") %></li>
                <li><%= i18n.t("ai.assist.capability.default-3") %></li>
                <% } %>
                <li><%= i18n.t("ai.assist.capability.human-controlled") %></li>
            </ul>
        </div>
        <nav class="ai-workbench-tabs" aria-label="AI workspace sections" data-workbench-tabs>
            <button type="button" data-tab-target="cv-module"><%= i18n.t("ai.assist.tab.cv") %></button>
            <button class="active" type="button" data-tab-target="chat-module"><%= i18n.t("ai.assist.tab.chat") %></button>
            <button type="button" data-tab-target="role-module"><%= currentRole == null ? i18n.t("ai.assist.tab.role") : i18n.t("ai.assist.tab.role-named", currentRole) %></button>
        </nav>
    </article>

    <div class="ai-workbench-page" id="cv-module" data-workbench-page>
    <div class="ai-stage-grid">
        <article class="panel ai-stage-panel">
            <div class="panel-header">
                <p class="eyebrow"><%= i18n.t("ai.assist.step.1") %></p>
                <h4><%= i18n.t("ai.assist.cv.heading") %></h4>
            </div>
            <% if ("TA".equals(currentRole)) { %>
            <div class="ai-status-strip">
                <span class="ai-source-chip <%= hasPdfCv ? "ai-source-llm" : "ai-source-local" %>">
                    <%= hasPdfCv ? i18n.t("ai.assist.cv.pdf-ready") : i18n.t("ai.assist.cv.pdf-required") %>
                </span>
                <span class="pill pill-neutral"><%= currentTaProfile == null || currentTaProfile.getCvPath() == null ? i18n.t("ai.assist.cv.no-linked") : currentTaProfile.getCvPath() %></span>
            </div>
            <form method="post" action="<%= request.getContextPath() %>/ta/profile" enctype="multipart/form-data" class="ai-upload-form">
                <input type="hidden" name="action" value="uploadCv">
                <input type="hidden" name="returnTo" value="/ai/assist">
                <label>
                    <%= i18n.t("ai.assist.cv.upload-label") %>
                    <input type="file" name="cvFile" accept=".pdf,.doc,.docx" required>
                </label>
                <button class="primary-button" type="submit"><%= i18n.t("ai.assist.cv.upload-button") %></button>
            </form>
            <p class="muted"><%= i18n.t("ai.assist.cv.upload-note") %></p>
            <% } else if ("MO".equals(currentRole)) { %>
            <div class="ai-evidence-box">
                <strong><%= i18n.t("ai.assist.cv.mo-heading") %></strong>
                <p><%= i18n.t("ai.assist.cv.mo-description") %></p>
            </div>
            <a class="secondary-button" href="<%= request.getContextPath() %>/mo/review"><%= i18n.t("ai.assist.cv.open-review") %></a>
            <% } else if ("ADMIN".equals(currentRole)) { %>
            <div class="ai-evidence-box">
                <strong><%= i18n.t("ai.assist.cv.admin-heading") %></strong>
                <p><%= i18n.t("ai.assist.cv.admin-description") %></p>
            </div>
            <a class="secondary-button" href="<%= request.getContextPath() %>/admin/workload"><%= i18n.t("ai.assist.cv.open-workload") %></a>
            <% } else { %>
            <div class="alert info"><%= i18n.t("ai.assist.cv.login-required") %></div>
            <% } %>
        </article>

        <article class="panel ai-stage-panel">
            <div class="panel-header">
                <p class="eyebrow"><%= i18n.t("ai.assist.runtime.eyebrow") %></p>
                <h4><%= i18n.t("ai.assist.runtime.heading") %></h4>
            </div>
            <div class="ai-evidence-box">
                <strong><%= i18n.t("ai.assist.runtime.readiness") %></strong>
                <p><%= i18n.t("ai.assist.runtime.description") %></p>
            </div>
            <ul class="feature-list">
                <% for (String item : aiTodos) { %>
                <li><%= i18n.td(item) %></li>
                <% } %>
            </ul>
        </article>
    </div>
    </div>

    <div class="ai-workbench-page active" id="chat-module" data-workbench-page>
    <div class="agent-workspace">
        <article class="panel agent-chat-panel">
            <div class="panel-header">
                <p class="eyebrow"><%= i18n.t("ai.assist.step.2") %></p>
                <h4><%= i18n.t("ai.assist.chat.heading") %></h4>
                <span class="ai-source-chip ai-source-llm"><%= i18n.t("ai.assist.chat.tool-calling") %></span>
            </div>
            <div class="agent-messages" data-agent-messages>
                <div class="agent-message agent-message-assistant">
                    <%= chatPrompt %>
                </div>
            </div>
            <form class="agent-chat-form" data-agent-form>
                <textarea name="message" rows="4" placeholder="<%= chatPlaceholder %>" data-agent-initial="<%= chatPrompt %>" required></textarea>
                <div class="button-row">
                    <button class="primary-button" type="submit"><%= i18n.t("common.send") %></button>
                    <button class="secondary-button" type="button" data-agent-clear><%= i18n.t("ai.assist.chat.clear") %></button>
                </div>
            </form>
            <p class="ai-disclaimer"><%= i18n.t("ai.assist.chat.disclaimer") %></p>
        </article>

        <article class="panel agent-trace-panel">
            <div class="panel-header">
                <p class="eyebrow"><%= i18n.t("ai.assist.trace.eyebrow") %></p>
                <h4><%= i18n.t("ai.assist.trace.heading") %></h4>
                <span class="ai-source-chip ai-source-local" data-agent-source><%= i18n.t("ai.assist.trace.idle") %></span>
            </div>
            <div class="agent-trace-list" data-agent-trace>
                <div class="ai-evidence-box"><%= i18n.t("ai.assist.trace.none-yet") %></div>
            </div>
            <div class="ai-evidence-box agent-final-json">
                <strong><%= i18n.t("ai.assist.trace.final-json") %></strong>
                <pre data-agent-final>{}</pre>
            </div>
        </article>
    </div>
    </div>

    <div class="ai-workbench-page" id="role-module" data-workbench-page>
    <article class="panel ai-role-panel">
        <div class="panel-header">
            <p class="eyebrow"><%= i18n.t("ai.assist.step.3") %></p>
            <h4><%= roleToolTitle %></h4>
        </div>
        <% if ("TA".equals(currentRole)) {
            List<JobPosting> openJobs = pageService.getOpenJobs();
        %>
        <div class="ai-role-grid">
            <div class="ai-evidence-box">
                <strong><%= i18n.t("ai.assist.role.my-skills") %></strong>
                <p><%= currentTaProfile == null ? i18n.t("ai.assist.role.profile-not-set") : currentTaProfile.getSkills() %></p>
            </div>
            <% for (JobPosting job : openJobs) {
                int score = pageService.calculateFitScore(currentUser.getLinkedId(), job.getId());
                List<String> missing = pageService.getMissingSkills(currentUser.getLinkedId(), job.getId());
            %>
            <div class="ai-evidence-box">
                <strong><%= job.getModuleCode() %> - <%= job.getTitle() %></strong>
                <p><%= i18n.t("ai.assist.role.fit-score", score) %></p>
                <p><%= i18n.t("ai.assist.role.missing-skills", missing.isEmpty() ? i18n.t("common.none") : String.join(", ", missing)) %></p>
                <a class="table-link" href="<%= request.getContextPath() %>/ta/job?id=<%= job.getId() %>"><%= i18n.t("ai.assist.role.open-job") %></a>
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
                    <th><%= i18n.t("ai.assist.role.job") %></th>
                    <th><%= i18n.t("ai.assist.role.applicants") %></th>
                    <th><%= i18n.t("ai.assist.role.suggested-prompt") %></th>
                </tr>
                </thead>
                <tbody>
                <% for (JobPosting job : moJobs) {
                    List<ApplicationRecord> apps = pageService.getApplicationsForJob(job.getId());
                %>
                <tr>
                    <td><%= job.getModuleCode() %> - <%= job.getTitle() %></td>
                    <td><%= apps.size() %></td>
                    <td><%= i18n.t("ai.assist.role.mo-prompt", job.getId()) %></td>
                </tr>
                <% } %>
                </tbody>
            </table>
        </div>
        <% } else if ("ADMIN".equals(currentRole)) {
            List<AiWorkloadAdvice> workloadAdvice = pageService.getAiWorkloadAdvice();
        %>
        <div class="ai-role-grid">
            <% for (AiWorkloadAdvice item : workloadAdvice) {
                String riskLabel = "at_cap".equals(item.getWorkloadRisk()) ? i18n.t("ai.assist.risk.at-cap") : ("caution".equals(item.getWorkloadRisk()) ? i18n.t("ai.assist.risk.caution") : i18n.t("ai.assist.risk.low"));
            %>
            <div class="ai-evidence-box">
                <strong><%= item.getTaName() %></strong>
                <p><%= i18n.t("ai.assist.role.accepted-jobs", item.getAcceptedCount()) %></p>
                <p><%= i18n.t("ai.assist.role.risk", riskLabel) %></p>
                <p><%= i18n.td(item.getAdvice()) %></p>
            </div>
            <% } %>
        </div>
        <% } else { %>
        <div class="alert info"><%= i18n.t("ai.assist.role.login-required") %></div>
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
        var initialPrompt = textarea.getAttribute("data-agent-initial") || "<%= i18n.t("ai.assist.chat.prompt.default") %>";
        var messages = workspace.querySelector("[data-agent-messages]");
        var trace = workspace.querySelector("[data-agent-trace]");
        var source = workspace.querySelector("[data-agent-source]");
        var finalJson = workspace.querySelector("[data-agent-final]");
        var endpoint = "<%= request.getContextPath() %>/ai/assist/chat";
        var clearEndpoint = "<%= request.getContextPath() %>/ai/assist/chat/clear";
        var hydratedMessages = <%= aiChatMessagesJson %>;
        var hydratedTrace = <%= aiChatTraceJson %>;
        var hydratedFinalJson = <%= aiChatFinalJson %>;
        var clearButton = workspace.querySelector("[data-agent-clear]");
        var tabs = document.querySelectorAll("[data-tab-target]");
        var pages = document.querySelectorAll("[data-workbench-page]");
        var labels = {
            noToolCallsReturned: "<%= i18n.t("ai.assist.js.no-tool-calls-returned") %>",
            clearFailed: "<%= i18n.t("ai.assist.js.clear-failed") %>",
            expectedJson: "<%= i18n.t("ai.assist.js.expected-json") %>",
            agentFailed: "<%= i18n.t("ai.assist.js.agent-failed") %>",
            agentCompleted: "<%= i18n.t("ai.assist.js.agent-completed") %>",
            chatFailed: "<%= i18n.t("ai.assist.js.chat-failed") %>",
            running: "<%= i18n.t("ai.assist.trace.running") %>",
            idle: "<%= i18n.t("ai.assist.trace.idle") %>",
            memory: "<%= i18n.t("ai.assist.trace.memory") %>",
            local: "<%= i18n.t("ai.assist.source.local") %>",
            llm: "<%= i18n.t("ai.assist.source.llm") %>",
            error: "<%= i18n.t("ai.assist.source.error") %>",
            unknown: "<%= i18n.t("common.none") %>",
            success: "<%= i18n.t("ai.assist.trace.success") %>",
            failed: "<%= i18n.t("ai.assist.trace.failed") %>"
        };

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
                empty.textContent = labels.noToolCallsReturned;
                trace.appendChild(empty);
                return;
            }
            items.forEach(function (item) {
                var box = document.createElement("div");
                box.className = "ai-evidence-box";
                var title = document.createElement("strong");
                title.textContent = item.tool + " - " + (item.success ? labels.success : labels.failed);
                var detail = document.createElement("pre");
                detail.textContent = JSON.stringify(item, null, 2);
                box.appendChild(title);
                box.appendChild(detail);
                trace.appendChild(box);
            });
        }

        function sourceLabel(mode) {
            if (mode === "local") return labels.local;
            if (mode === "llm") return labels.llm;
            if (mode === "error") return labels.error;
            return mode || labels.unknown;
        }

        function renderHydratedMemory() {
            if (!hydratedMessages || !hydratedMessages.length) {
                return;
            }
            messages.innerHTML = "";
            hydratedMessages.forEach(function (item) {
                var role = item && item.role === "user" ? "agent-message-user" : "agent-message-assistant";
                addMessage(item && item.content ? item.content : "", role);
            });
            renderTrace(hydratedTrace || []);
            finalJson.textContent = JSON.stringify(hydratedFinalJson || {}, null, 2);
            var sourceMode = hydratedFinalJson && hydratedFinalJson.sourceMode ? hydratedFinalJson.sourceMode : "memory";
            source.textContent = sourceMode === "memory" ? labels.memory : sourceLabel(sourceMode);
            source.className = "ai-source-chip ai-source-" + (sourceMode === "memory" ? "local" : sourceMode === "error" ? "error" : "llm");
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
            return labels.agentCompleted;
        }

        function resetChatUi() {
            messages.innerHTML = "";
            addMessage(initialPrompt, "agent-message-assistant");
            source.textContent = labels.idle;
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
                return response.json();
            }).then(function () {
                hydratedMessages = [];
                hydratedTrace = [];
                hydratedFinalJson = {};
                resetChatUi();
            }).catch(function (error) {
                addMessage(labels.clearFailed + ": " + error.message, "agent-message-assistant");
            });
        });

        renderHydratedMemory();

        form.addEventListener("submit", function (event) {
            event.preventDefault();
            var text = textarea.value.trim();
            if (!text) {
                return;
            }
            addMessage(text, "agent-message-user");
            textarea.value = "";
            source.textContent = labels.running;
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
                        throw new Error(labels.expectedJson + " " + endpoint + " HTTP "
                            + response.status + ": " + body.substring(0, 160));
                    }
                });
            }).then(function (data) {
                source.textContent = sourceLabel(data.sourceMode);
                source.className = "ai-source-chip ai-source-" + (data.sourceMode === "error" ? "error" : "llm");
                addMessage(data.success ? resolveAgentReply(data) : (data.errorMessage || labels.agentFailed), "agent-message-assistant");
                renderTrace(data.toolTrace || []);
                finalJson.textContent = JSON.stringify(data.finalJson || {}, null, 2);
            }).catch(function (error) {
                source.textContent = labels.error;
                source.className = "ai-source-chip ai-source-error";
                addMessage(labels.chatFailed + ": " + error.message, "agent-message-assistant");
            });
        });
    })();
</script>
<%@ include file="../fragments/pageEnd.jspf" %>
