package com.example.pinterest_clone_test2.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Model dữ liệu cho pin bị báo cáo, kết hợp thông tin pin và các báo cáo của nó
 */
public class ReportedPin {
    private String reportId;
    private String pinId;
    private String pinTitle;
    private String pinDescription;
    private String pinAuthorId;
    private String pinAuthorName;
    private String pinAuthorAvatar;
    private String mediaUrl;
    private String thumbnailUrl;
    private long pinCreatedAt;
    private int reportCount;
    private boolean isChecked;
    private long lastReportedAt;
    private Map<String, Integer> reasonsCount;
    private List<String> reporterIds;
    private String mostCommonReasonId;
    private String mostCommonReasonTitle;
    private Pin.PinType pinType;

    public ReportedPin() {
        reasonsCount = new HashMap<>();
        reporterIds = new ArrayList<>();
    }

    // Getters
    public String getReportId() {
        return reportId;
    }

    public String getPinId() {
        return pinId;
    }

    public String getPinTitle() {
        return pinTitle;
    }

    public String getPinDescription() {
        return pinDescription;
    }

    public String getPinAuthorId() {
        return pinAuthorId;
    }

    public String getPinAuthorName() {
        return pinAuthorName;
    }

    public String getPinAuthorAvatar() {
        return pinAuthorAvatar;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public long getPinCreatedAt() {
        return pinCreatedAt;
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

    public Pin.PinType getPinType() {
        return pinType;
    }

    // Setters (Builder pattern)
    public ReportedPin setReportId(String reportId) {
        this.reportId = reportId;
        return this;
    }

    public ReportedPin setPinId(String pinId) {
        this.pinId = pinId;
        return this;
    }

    public ReportedPin setPinTitle(String pinTitle) {
        this.pinTitle = pinTitle;
        return this;
    }

    public ReportedPin setPinDescription(String pinDescription) {
        this.pinDescription = pinDescription;
        return this;
    }

    public ReportedPin setPinAuthorId(String pinAuthorId) {
        this.pinAuthorId = pinAuthorId;
        return this;
    }

    public ReportedPin setPinAuthorName(String pinAuthorName) {
        this.pinAuthorName = pinAuthorName;
        return this;
    }

    public ReportedPin setPinAuthorAvatar(String pinAuthorAvatar) {
        this.pinAuthorAvatar = pinAuthorAvatar;
        return this;
    }

    public ReportedPin setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
        return this;
    }

    public ReportedPin setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
        return this;
    }

    public ReportedPin setPinCreatedAt(long pinCreatedAt) {
        this.pinCreatedAt = pinCreatedAt;
        return this;
    }

    public ReportedPin setReportCount(int reportCount) {
        this.reportCount = reportCount;
        return this;
    }

    public ReportedPin setChecked(boolean checked) {
        isChecked = checked;
        return this;
    }

    public ReportedPin setLastReportedAt(long lastReportedAt) {
        this.lastReportedAt = lastReportedAt;
        return this;
    }

    public ReportedPin setReasonsCount(Map<String, Integer> reasonsCount) {
        this.reasonsCount = reasonsCount;
        return this;
    }

    public ReportedPin setReporterIds(List<String> reporterIds) {
        this.reporterIds = reporterIds;
        return this;
    }

    public ReportedPin setMostCommonReasonId(String mostCommonReasonId) {
        this.mostCommonReasonId = mostCommonReasonId;
        return this;
    }

    public ReportedPin setMostCommonReasonTitle(String mostCommonReasonTitle) {
        this.mostCommonReasonTitle = mostCommonReasonTitle;
        return this;
    }

    public ReportedPin setPinType(Pin.PinType pinType) {
        this.pinType = pinType;
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
     * Thêm một báo cáo mới vào pin này
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