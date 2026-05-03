package com.group91.tars.model;

public class OperationResult {
    private boolean success;
    private String message;
    private String messageKey;
    private Object[] messageArgs;

    public OperationResult() {
    }

    public OperationResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public static OperationResult success(String message) {
        return new OperationResult(true, message);
    }

    public static OperationResult failure(String message) {
        return new OperationResult(false, message);
    }

    public static OperationResult success(String messageKey, String fallback, Object... args) {
        OperationResult r = new OperationResult();
        r.success = true;
        r.messageKey = messageKey;
        r.message = fallback;
        r.messageArgs = args;
        return r;
    }

    public static OperationResult failure(String messageKey, String fallback, Object... args) {
        OperationResult r = new OperationResult();
        r.success = false;
        r.messageKey = messageKey;
        r.message = fallback;
        r.messageArgs = args;
        return r;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public void setMessageKey(String messageKey) {
        this.messageKey = messageKey;
    }

    public Object[] getMessageArgs() {
        return messageArgs;
    }

    public void setMessageArgs(Object[] messageArgs) {
        this.messageArgs = messageArgs;
    }
}
