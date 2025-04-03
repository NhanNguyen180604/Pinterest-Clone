package com.example.pinterest_clone_test2.models;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.databinding.library.baseAdapters.BR;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReportReason extends BaseObservable {
    String _id;
    String _title;
    String _description;

    public ReportReason(String id, String title, String description) {
        _id = id;
        _title = title;
        _description = description;
    }

    public ReportReason(ReportReason other) {
        _id = other.getId();
        _title = other.getTitle();
        _description = other.getDescription();
    }

    @Bindable
    public String getId() {
        return _id;
    }

    public void setId(String id) {
        _id = id;
        notifyPropertyChanged(BR.id);
    }

    @Bindable
    public String getTitle() {
        return _title;
    }

    public void setTitle(String title) {
        _title = title;
        notifyPropertyChanged(BR.title);
    }

    @Bindable
    public String getDescription() {
        return _description;
    }

    public void setDescription(String description) {
        _description = description;
        notifyPropertyChanged(BR.description);
    }

    public static List<ReportReason> Reasons = new ArrayList<>(Arrays.asList(
            new ReportReason("report-reason-01", "Spam", "Lừa gạt hoặc cố tình đăng nhiều lần"),
            new ReportReason("report-reason-02", "Khỏa thân hoặc khiêu dâm", "Chứa nội dung khỏa thân, khiêu dâm, tục tĩu"),
            new ReportReason("report-reason-03", "Ngôn từ gây thù ghét", "Kêu gọi ghét bỏ cá nhân hoặc tổ chức"),
            new ReportReason("report-reason-04", "Quấy rối hoặc bắt nạt", "Đe dọa, nhục mạ người khách"),
            new ReportReason("report-reason-05", "Thông tin giả", "Lan truyền thông tin sai lệch"),
            new ReportReason("report-reason-06", "Ngược đãi bản thân", "Xúi giục hành vi tự hại, tự sát"),
            new ReportReason("report-reason-07", "Bạo lực", "Khoắc họa hoặc tôn vinh bạo lực, bao gồm đe dọa hoặc hình ảnh bạo lực, máu me"),
            new ReportReason("report-reason-08", "Vật dụng nguy hiểm", "Quảng cáo, kinh doanh chất cấm, súng đạn...")
    ));
}
