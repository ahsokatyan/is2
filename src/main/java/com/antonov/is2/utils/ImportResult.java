package com.antonov.is2.utils;

public class ImportResult {
    private final boolean success;
    private final String message;
    private final int addedCount;

    public ImportResult(boolean success, String message, int addedCount) {
        this.success = success;
        this.message = message;
        this.addedCount = addedCount;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public int getAddedCount() {
        return addedCount;
    }
}
