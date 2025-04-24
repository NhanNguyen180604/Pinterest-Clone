package com.example.pinterest_clone_test2.models;

/**
 * Model báo cáo ưu tiên cần xử lý nhanh
 */
public class PriorityReport {
    public enum ReportType {
        PIN,
        COMMENT
    }

    private String reportId;
    private ReportType reportType;
    private String contentId;
    private String contentTitle;
    private String contentThumbnail;
    private int reportCount;
    private String mainReasonId;
    private String mainReasonTitle;
    private long firstReportedAt;
    private long lastReportedAt;
    private ReportSeverity severity;
    private String authorId;
    private String authorName;
    private String authorAvatar;

    public PriorityReport() {
    }

    // Getters
    public String getReportId() {
        return reportId;
    }

    public ReportType getReportType() {
        return reportType;
    }

    public String getContentId() {
        return contentId;
    }

    public String getContentTitle() {
        return contentTitle;
    }

    public String getContentThumbnail() {
        return contentThumbnail;
    }

    public int getReportCount() {
        return reportCount;
    }

    public String getMainReasonId() {
        return mainReasonId;
    }

    public String getMainReasonTitle() {
        return mainReasonTitle;
    }

    public long getFirstReportedAt() {
        return firstReportedAt;
    }

    public long getLastReportedAt() {
        return lastReportedAt;
    }

    public ReportSeverity getSeverity() {
        return severity;
    }

    public String getAuthorId() {
        return authorId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public String getAuthorAvatar() {
        return authorAvatar;
    }

    // Setters (Builder pattern)
    public PriorityReport setReportId(String reportId) {
        this.reportId = reportId;
        return this;
    }

    public PriorityReport setReportType(ReportType reportType) {
        this.reportType = reportType;
        return this;
    }

    public PriorityReport setContentId(String contentId) {
        this.contentId = contentId;
        return this;
    }

    public PriorityReport setContentTitle(String contentTitle) {
        this.contentTitle = contentTitle;
        return this;
    }

    public PriorityReport setContentThumbnail(String contentThumbnail) {
        this.contentThumbnail = contentThumbnail;
        return this;
    }

    public PriorityReport setReportCount(int reportCount) {
        this.reportCount = reportCount;
        return this;
    }

    public PriorityReport setMainReasonId(String mainReasonId) {
        this.mainReasonId = mainReasonId;
        return this;
    }

    public PriorityReport setMainReasonTitle(String mainReasonTitle) {
        this.mainReasonTitle = mainReasonTitle;
        return this;
    }

    public PriorityReport setFirstReportedAt(long firstReportedAt) {
        this.firstReportedAt = firstReportedAt;
        return this;
    }

    public PriorityReport setLastReportedAt(long lastReportedAt) {
        this.lastReportedAt = lastReportedAt;
        return this;
    }

    public PriorityReport setSeverity(ReportSeverity severity) {
        this.severity = severity;
        return this;
    }

    public PriorityReport setAuthorId(String authorId) {
        this.authorId = authorId;
        return this;
    }

    public PriorityReport setAuthorName(String authorName) {
        this.authorName = authorName;
        return this;
    }

    public PriorityReport setAuthorAvatar(String authorAvatar) {
        this.authorAvatar = authorAvatar;
        return this;
    }

    /**
     * Tính thời gian chờ xử lý (tính từ báo cáo đầu tiên đến hiện tại)
     * @return Thời gian chờ tính bằng milli giây
     */
    public long getWaitingTime() {
        return System.currentTimeMillis() - firstReportedAt;
    }

    /**
     * Kiểm tra xem báo cáo có cần xử lý ngay không dựa trên mức độ nghiêm trọng và thời gian chờ
     * @return true nếu cần xử lý ngay
     */
    public boolean needsImmediateAction() {
        // Nếu độ nghiêm trọng là URGENT, luôn cần xử lý ngay
        if (severity == ReportSeverity.URGENT) {
            return true;
        }

        // Thời gian chờ tối đa (ms) theo độ nghiêm trọng
        long maxWaitingTime;
        switch (severity) {
            case HIGH:
                maxWaitingTime = 24 * 60 * 60 * 1000; // 24 giờ
                break;
            case MEDIUM:
                maxWaitingTime = 3 * 24 * 60 * 60 * 1000; // 3 ngày
                break;
            case LOW:
            default:
                maxWaitingTime = 7 * 24 * 60 * 60 * 1000; // 7 ngày
                break;
        }

        return getWaitingTime() > maxWaitingTime;
    }
}