package com.group91.tars.service.ai.tool;

import com.group91.tars.model.UserAccount;
import com.group91.tars.service.ai.CvTextExtractor;
import com.group91.tars.service.ai.LocalRuleAiEngine;

public class RecruitmentToolRegistryFactory {
    private final CvTextExtractor cvTextExtractor;
    private final LocalRuleAiEngine localRuleAiEngine;

    public RecruitmentToolRegistryFactory(CvTextExtractor cvTextExtractor, LocalRuleAiEngine localRuleAiEngine) {
        this.cvTextExtractor = cvTextExtractor;
        this.localRuleAiEngine = localRuleAiEngine;
    }

    public AgentToolRegistry create(UserAccount currentUser) {
        RecruitmentToolSupport support = new RecruitmentToolSupport(currentUser);
        AgentToolRegistry registry = new AgentToolRegistry();
        registry.register(new GetTaProfileTool(support));
        registry.register(new GetJobPostingTool(support));
        registry.register(new ListOpenJobsTool(support));
        registry.register(new ListManagedJobsTool(support));
        registry.register(new ExtractCvTextTool(support, cvTextExtractor));
        registry.register(new CalculateFitScoreTool(support, localRuleAiEngine));
        registry.register(new GetWorkloadStatusTool(support));
        registry.register(new ListJobApplicantsTool(support));
        return registry;
    }
}
