package com.group91.tars.model;

/**
 * Represents the result of a business operation, carrying a success flag,
 * a human-readable message, an optional i18n message key, and optional
 * message format arguments. Controllers use the messageKey to perform
 * locale-aware flash messaging via {@code flashI18n()}.
 */
public class OperationResult {
    private boolean success;
    private String message;
    private String messageKey;
    private Object[] messageArgs;

    public OperationResult() {
    }

    /**
     * Constructs an OperationResult with a success flag and a plain-text message.
     *
     * @param success whether the operation succeeded
     * @param message the human-readable result message
     */
    public OperationResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    /**
     * Creates a successful result with an i18n message key and fallback text.
     *
     * @param messageKey the i18n property key for locale-aware translation
     * @param fallback   the fallback message text used when i18n lookup fails
     * @param args       optional {@link java.text.MessageFormat} arguments for the message
     * @return a successful OperationResult
     */
    public static OperationResult success(String messageKey, String fallback, Object... args) {
        OperationResult r = new OperationResult();
        r.success = true;
        r.messageKey = messageKey;
        r.message = fallback;
        r.messageArgs = args;
        return r;
    }

    /**
     * Creates a failure result with an i18n message key and fallback text.
     *
     * @param messageKey the i18n property key for locale-aware translation
     * @param fallback   the fallback message text used when i18n lookup fails
     * @param args       optional {@link java.text.MessageFormat} arguments for the message
     * @return a failure OperationResult
     */
    public static OperationResult failure(String messageKey, String fallback, Object... args) {
        OperationResult r = new OperationResult();
        r.success = false;
        r.messageKey = messageKey;
        r.message = fallback;
        r.messageArgs = args;
        return r;
    }

    /**
     * Returns whether the operation succeeded.
     *
     * @return true if the operation succeeded, false otherwise
     */
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    /**
     * Returns the human-readable result message (fallback text).
     *
     * @return the message string
     */
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Returns the i18n property key for locale-aware translation, or null if not set.
     *
     * @return the message key, or null
     */
    public String getMessageKey() {
        return messageKey;
    }

    public void setMessageKey(String messageKey) {
        this.messageKey = messageKey;
    }

    /**
     * Returns the optional message format arguments.
     *
     * @return the argument array, or null
     */
    public Object[] getMessageArgs() {
        return messageArgs;
    }

    public void setMessageArgs(Object[] messageArgs) {
        this.messageArgs = messageArgs;
    }
}
