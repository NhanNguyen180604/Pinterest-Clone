package com.example.pinterest_clone_test2.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Mô hình dữ liệu cho comment bị báo cáo, kết hợp thông tin comment và các báo cáo của nó
 */
public class ReportedComment {
    private String reportId;
    private String commentId;
    private String commentContent;
    private String commentAuthorId;
    private String commentAuthorName;
    private String commentAuthorAvatar;
    private String attachmentUrl;
    private String pinId;
    private long commentCreatedAt;
    private int reportCount;
    private boolean isChecked;
    private long lastReportedAt;
    private Map<String, Integer> reasonsCount;
    private List<String> reporterIds;
    private String mostCommonReasonId;
    private String mostCommonReasonTitle;

    public ReportedComment() {
        reasonsCount = new HashMap<>();
        reporterIds = new ArrayList<>();
    }

    // Getters
    public String getReportId() {
        return reportId;
    }

    public String getCommentId() {
        return commentId;
    }

    public String getCommentContent() {
        return commentContent;
    }

    public String getCommentAuthorId() {
        return commentAuthorId;
    }

    public String getCommentAuthorName() {
        return commentAuthorName;
    }

    public String getCommentAuthorAvatar() {
        return commentAuthorAvatar;
    }

    public String getAttachmentUrl() {
        return attachmentUrl;
    }

    public String getPinId() {
        return pinId;
    }

    public long getCommentCreatedAt() {
        return commentCreatedAt;
    }

    public int getReportCount() {
        return reportCount;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public long getLastReportedAt() {
        return lastReportedAt;
    }

    public Map<String, Integer> getReasonsCount() {
        return reasonsCount;
    }

    public List<String> getReporterIds() {
        return reporterIds;
    }

    public String getMostCommonReasonId() {
        return mostCommonReasonId;
    }

    public String getMostCommonReasonTitle() {
        return mostCommonReasonTitle;
    }

    // Setters (Builder pattern)
    public ReportedComment setReportId(String reportId) {
        this.reportId = reportId;
        return this;
    }

    public ReportedComment setCommentId(String commentId) {
        this.commentId = commentId;
        return this;
    }

    public ReportedComment setCommentContent(String commentContent) {
        this.commentContent = commentContent;
        return this;
    }

    public ReportedComment setCommentAuthorId(String commentAuthorId) {
        this.commentAuthorId = commentAuthorId;
        return this;
    }

    public ReportedComment setCommentAuthorName(String commentAuthorName) {
        this.commentAuthorName = commentAuthorName;
        return this;
    }

    public ReportedComment setCommentAuthorAvatar(String commentAuthorAvatar) {
        this.commentAuthorAvatar = commentAuthorAvatar;
        return this;
    }

    public ReportedComment setAttachmentUrl(String attachmentUrl) {
        this.attachmentUrl = attachmentUrl;
        return this;
    }

    public ReportedComment setPinId(String pinId) {
        this.pinId = pinId;
        return this;
    }

    public ReportedComment setCommentCreatedAt(long commentCreatedAt) {
        this.commentCreatedAt = commentCreatedAt;
        return this;
    }

    public ReportedComment setReportCount(int reportCount) {
        this.reportCount = reportCount;
        return this;
    }

    public ReportedComment setChecked(boolean checked) {
        isChecked = checked;
        return this;
    }

    public ReportedComment setLastReportedAt(long lastReportedAt) {
        this.lastReportedAt = lastReportedAt;
        return this;
    }

    public ReportedComment setReasonsCount(Map<String, Integer> reasonsCount) {
        this.reasonsCount = reasonsCount;
        return this;
    }

    public ReportedComment setReporterIds(List<String> reporterIds) {
        this.reporterIds = reporterIds;
        return this;
    }

    public ReportedComment setMostCommonReasonId(String mostCommonReasonId) {
        this.mostCommonReasonId = mostCommonReasonId;
        return this;
    }

    public ReportedComment setMostCommonReasonTitle(String mostCommonReasonTitle) {
        this.mostCommonReasonTitle = mostCommonReasonTitle;
        return this;
    }

    /**
     * Cập nhật lý do báo cáo phổ biến nhất dựa vào reasonsCount
     */
    public void updateMostCommonReason() {
        int maxCount = 0;
        String maxReasonId = "";

        for (Map.Entry<String, Integer> entry : reasonsCount.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                maxReasonId = entry.getKey();
            }
        }

        this.mostCommonReasonId = maxReasonId;
    }

    /**
     * Thêm một báo cáo mới vào comment này
     */
    public void addReport(String reporterId, List<ReportReason> reasons, long reportedAt) {
        if (!reporterIds.contains(reporterId)) {
            reporterIds.add(reporterId);
        }

        // Cập nhật số lượng của mỗi lý do
        for (ReportReason reason : reasons) {
            String reasonId = reason.getId();
            Integer count = reasonsCount.getOrDefault(reasonId, 0);
            reasonsCount.put(reasonId, count + 1);

            // Cập nhật tiêu đề của lý do
            if (reasonId.equals(mostCommonReasonId)) {
                mostCommonReasonTitle = reason.getTitle();
            }
        }

        // Cập nhật thời gian báo cáo gần nhất
        if (reportedAt > lastReportedAt) {
            lastReportedAt = reportedAt;
        }

        // Cập nhật số lượng báo cáo
        reportCount = reporterIds.size();

        // Cập nhật lý do phổ biến nhất
        updateMostCommonReason();
    }
}