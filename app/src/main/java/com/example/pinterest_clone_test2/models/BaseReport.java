package com.example.pinterest_clone_test2.models;

import androidx.annotation.NonNull;

import java.util.List;
import java.util.stream.Collectors;

public abstract class BaseReport {
    protected final List<ReportReason> _reasons;
    protected final String _reportOwnerId;

    // the id of reported object, could be a pin, user, comment
    protected final String _reportedId;

    public BaseReport(@NonNull List<ReportReason> reasons, @NonNull String reportOwnerId, @NonNull String reportedId) {
        _reasons = reasons;
        _reportOwnerId = reportOwnerId;
        _reportedId = reportedId;
    }

    @Override
    @NonNull
    public String toString() {
        if (_reasons.isEmpty()) {
            return "";
        }

        return _reasons.stream()
                .map(ReportReason::getDescription)
                .collect(Collectors.joining(", "));
    }

    public String getReportOwnerId() {
        return _reportOwnerId;
    }

    public String getReportedId() {
        return _reportedId;
    }

    abstract void sendReportToDatabase();
}
