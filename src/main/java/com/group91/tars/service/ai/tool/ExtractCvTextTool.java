package com.group91.tars.service.ai.tool;

import com.google.gson.JsonObject;
import com.group91.tars.model.TAProfile;
import com.group91.tars.service.ai.CvTextExtractor;

public class ExtractCvTextTool implements AgentTool {
    private final RecruitmentToolSupport support;
    private final CvTextExtractor extractor;

    public ExtractCvTextTool(RecruitmentToolSupport support, CvTextExtractor extractor) {
        this.support = support;
        this.extractor = extractor;
    }

    public String getName() {
        return "extract_cv_text";
    }

    public String getDescription() {
        return "Extract text from the TA's uploaded PDF CV for this analysis only. Does not persist CV text.";
    }

    public JsonObject getParametersSchema() {
        return support.schema("taId");
    }

    public AgentToolResult execute(JsonObject arguments) {
        String taId = support.requiredString(arguments, "taId");
        if (taId == null) {
            return AgentToolResult.failure(getName(), "Missing required argument: taId");
        }
        if (!support.canReadCv(taId)) {
            return AgentToolResult.failure(getName(), "Permission denied for CV text: " + taId);
        }
        TAProfile profile = support.findProfile(taId);
        if (profile == null) {
            return AgentToolResult.failure(getName(), "TA profile not found: " + taId);
        }
        String text = extractor.extract(profile);
        JsonObject data = new JsonObject();
        data.addProperty("available", text != null
            && !"CV not available.".equals(text)
            && !"CV analysis supports PDF files only.".equals(text)
            && !"CV could not be parsed.".equals(text));
        data.addProperty("text", text);
        data.addProperty("evidenceLength", text == null ? 0 : text.length());
        return AgentToolResult.success(getName(), data);
    }
}
