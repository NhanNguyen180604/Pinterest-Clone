package com.example.pinterest_clone_test2.models;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.databinding.library.baseAdapters.BR;

public class ReportReason extends BaseObservable {
    String _id;
    String _title;
    String _description;

    public ReportReason(String id, String title, String description) {
        _id = id;
        _title = title;
        _description = description;
    }

    public ReportReason(ReportReason other){
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
}
