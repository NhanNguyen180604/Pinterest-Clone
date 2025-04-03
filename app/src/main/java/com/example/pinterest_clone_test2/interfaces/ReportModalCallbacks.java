package com.example.pinterest_clone_test2.interfaces;

import androidx.annotation.NonNull;

import com.example.pinterest_clone_test2.models.BaseReport;
import com.example.pinterest_clone_test2.models.ReportReason;

import java.util.List;

public interface ReportModalCallbacks {
    void CreateReport(@NonNull List<ReportReason> reasons);
}
