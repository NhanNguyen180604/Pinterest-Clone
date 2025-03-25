package com.example.pinterest_clone_test2.daos;

import androidx.annotation.NonNull;

import com.example.pinterest_clone_test2.models.ReportReason;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MockReportReasonDao implements IReportReasonDao {
    List<ReportReason> _reportReasons;

    public MockReportReasonDao() {
        _reportReasons = new ArrayList<>(Arrays.asList(
                new ReportReason("report-reason-01", "Spam", "Misleading or repetitive post"),
                new ReportReason("report-reason-02", "Nudity or Pornography", "Contains nudity, pornography, or sexually explicit content"),
                new ReportReason("report-reason-03", "Hate Speech", "Promotes hate against individuals or groups"),
                new ReportReason("report-reason-04", "Harassment or Bullying", "Targeting someone with threats, humiliation, or intimidation"),
                new ReportReason("report-reason-05", "Misinformation", "Contains false or misleading information"),
                new ReportReason("report-reason-06", "Self-harm", "Encourages or glorifies self-harm or suicide"),
                new ReportReason("report-reason-07", "Violence", "Depicts or glorifies violence, including threats or graphic content"),
                new ReportReason("report-reason-08", "Dangerous Products", "Promotes or sells unsafe products or substances")
        ));
    }

    // return deep copy list
    @Override
    public List<ReportReason> getReasons() {
        List<ReportReason> copiedList = new ArrayList<>();
        for (ReportReason reason :
                _reportReasons) {
            copiedList.add(new ReportReason(reason));
        }
        return copiedList;
    }

    @Override
    public void addReason(@NonNull ReportReason reportReason) {
        _reportReasons.add(reportReason);
    }

    @Override
    public void removeReason(@NonNull ReportReason reportReason) {
        _reportReasons.remove(reportReason);
    }
}
