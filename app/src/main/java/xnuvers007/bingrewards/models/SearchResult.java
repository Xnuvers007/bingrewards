package xnuvers007.bingrewards.models;

import java.util.Date;

public class SearchResult {
    private String keyword;
    private boolean success;
    private String errorMessage;
    private Date timestamp;
    private int searchNumber;
    private long responseTime;

    public SearchResult() {
        this.timestamp = new Date();
    }

    public SearchResult(String keyword, boolean success, int searchNumber) {
        this.keyword = keyword;
        this.success = success;
        this.searchNumber = searchNumber;
        this.timestamp = new Date();
    }

    // Getters
    public String getKeyword() {
        return keyword;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public int getSearchNumber() {
        return searchNumber;
    }

    public long getResponseTime() {
        return responseTime;
    }

    // Setters
    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public void setSearchNumber(int searchNumber) {
        this.searchNumber = searchNumber;
    }

    public void setResponseTime(long responseTime) {
        this.responseTime = responseTime;
    }

    @Override
    public String toString() {
        return "SearchResult{" +
                "keyword='" + keyword + '\'' +
                ", success=" + success +
                ", searchNumber=" + searchNumber +
                ", timestamp=" + timestamp +
                '}';
    }
}