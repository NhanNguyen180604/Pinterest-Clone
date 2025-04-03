package com.example.pinterest_clone_test2.models;

import androidx.annotation.NonNull;

import java.util.List;

public class PinReport extends BaseReport{
    public PinReport(@NonNull List<ReportReason> reasons, @NonNull String reportOwnerId, @NonNull String pinId) {
        super(reasons, reportOwnerId, pinId);
    }

    @Override
    public void sendReportToDatabase() {
        //TODO: send pin report to database
    }
}
