package com.example.pinterest_clone_test2.models;

import androidx.annotation.NonNull;

import java.util.List;

public class CommentReport extends BaseReport {
    public CommentReport(@NonNull List<ReportReason> reasons, @NonNull String reportOwnerId, @NonNull Comment comment) {
        super(reasons, reportOwnerId, comment.getId());
    }

    @Override
    public void sendReportToDatabase() {
        // TODO send this report to the database
    }
}
