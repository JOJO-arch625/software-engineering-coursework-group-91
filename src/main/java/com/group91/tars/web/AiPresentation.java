package com.group91.tars.web;

import com.group91.tars.i18n.I18n;

/**
 * Shared labels for AI advisory panels so JSP pages stay consistent with {@link I18n}.
 */
public final class AiPresentation {
    private AiPresentation() {
    }

    /**
     * Returns a human-readable label for the AI result source mode shown in the UI.
     *
     * @param i18n       the active locale bundle
     * @param sourceMode the mode stored on an AI result (local, llm, llm_tool, error, etc.)
     * @return a localized source label
     */
    public static String sourceLabel(I18n i18n, String sourceMode) {
        if (sourceMode == null || sourceMode.trim().isEmpty()) {
            return i18n.t("ai.source.local");
        }
        if ("llm_tool".equals(sourceMode)) {
            return i18n.t("ai.source.llm-tool");
        }
        if ("llm".equals(sourceMode)) {
            return i18n.t("ai.source.llm");
        }
        if ("error".equals(sourceMode)) {
            return i18n.t("ai.source.error");
        }
        return i18n.t("ai.source.local");
    }
}
