package com.example.pinterest_clone_test2.models;

/**
 * Định nghĩa các mức độ nghiêm trọng của báo cáo
 */
public enum ReportSeverity {
    LOW(1),
    MEDIUM(2),
    HIGH(3),
    URGENT(4);

    private final int value;

    ReportSeverity(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    /**
     * Xác định mức độ nghiêm trọng dựa trên loại báo cáo và số lượng báo cáo
     */
    public static ReportSeverity determineSeverity(String reasonId, int reportCount) {
        // Các lý do nghiêm trọng
        boolean isCriticalReason = reasonId.equals("report-reason-02") || // Nudity
                reasonId.equals("report-reason-03") || // Hate speech
                reasonId.equals("report-reason-06") || // Self harm
                reasonId.equals("report-reason-07");   // Violence

        // Các lý do trung bình
        boolean isMediumReason = reasonId.equals("report-reason-04") || // Harassment
                reasonId.equals("report-reason-05") || // False info
                reasonId.equals("report-reason-08");   // Dangerous goods

        // Xác định mức độ nghiêm trọng dựa trên loại và số lượng
        if (isCriticalReason) {
            if (reportCount >= 3) {
                return URGENT;
            } else {
                return HIGH;
            }
        } else if (isMediumReason) {
            if (reportCount >= 5) {
                return HIGH;
            } else {
                return MEDIUM;
            }
        } else {
            // Spam và các lý do khác
            if (reportCount >= 10) {
                return HIGH;
            } else if (reportCount >= 5) {
                return MEDIUM;
            } else {
                return LOW;
            }
        }
    }
}