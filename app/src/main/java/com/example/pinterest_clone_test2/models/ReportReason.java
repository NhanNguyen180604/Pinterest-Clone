package com.example.pinterest_clone_test2.models;

import android.content.Context;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.databinding.library.baseAdapters.BR;

import com.example.pinterest_clone_test2.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

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

    public static List<ReportReason> GetReasons(Context context) {
        return new ArrayList<>(Arrays.asList(
                new ReportReason("report-reason-01",
                        context.getString(R.string.reason_spam),
                        context.getString(R.string.desc_spam)),
                new ReportReason("report-reason-02",
                        context.getString(R.string.reason_nudity),
                        context.getString(R.string.desc_nudity)),
                new ReportReason("report-reason-03",
                        context.getString(R.string.reason_hate_speech),
                        context.getString(R.string.desc_hate_speech)),
                new ReportReason("report-reason-04",
                        context.getString(R.string.reason_harassment),
                        context.getString(R.string.desc_harassment)),
                new ReportReason("report-reason-05",
                        context.getString(R.string.reason_false_info),
                        context.getString(R.string.desc_false_info)),
                new ReportReason("report-reason-06",
                        context.getString(R.string.reason_self_harm),
                        context.getString(R.string.desc_self_harm)),
                new ReportReason("report-reason-07",
                        context.getString(R.string.reason_violence),
                        context.getString(R.string.desc_violence)),
                new ReportReason("report-reason-08",
                        context.getString(R.string.reason_dangerous_goods),
                        context.getString(R.string.desc_dangerous_goods))
        ));
    }

    /**
     * Gets the report reason prefix from resources.
     * Uses a common string key for both languages.
     */
    public static String getReasonPrefix(Context context) {
        return context.getString(R.string.reason_prefix);
    }
}