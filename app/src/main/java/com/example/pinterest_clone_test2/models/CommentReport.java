package com.example.pinterest_clone_test2.models;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.List;

public class CommentReport extends BaseReport {

    public CommentReport(@NonNull List<ReportReason> reasons, @NonNull String reportOwnerId, @NonNull Comment comment, Context context) {
        super(reasons, reportOwnerId, comment.getId(), context);
        reportData.put("type", "COMMENT");
    }
}
