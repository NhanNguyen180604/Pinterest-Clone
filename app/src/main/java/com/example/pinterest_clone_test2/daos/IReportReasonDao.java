package com.example.pinterest_clone_test2.daos;

import androidx.annotation.NonNull;

import com.example.pinterest_clone_test2.models.Comment;
import com.example.pinterest_clone_test2.models.ReportReason;

import java.util.List;

public interface IReportReasonDao {
    List<ReportReason> getReasons();

    void addReason(@NonNull ReportReason reportReason);

    void removeReason(@NonNull ReportReason reportReason);
}
