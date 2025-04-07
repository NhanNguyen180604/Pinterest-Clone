package com.example.pinterest_clone_test2.models;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.List;

public class PinReport extends BaseReport {

    public PinReport(@NonNull List<ReportReason> reasons, @NonNull String reportOwnerId, @NonNull String pinId, Context context) {
        super(reasons, reportOwnerId, pinId, context);
        reportData.put("type", "PIN");
    }
}
