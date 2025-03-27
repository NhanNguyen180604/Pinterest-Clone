package com.example.pinterest_clone_test2.ui.admin.manage_comment;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ManageCommentViewModel extends ViewModel {
    private final MutableLiveData<String> mText;

    public ManageCommentViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is manage comment fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}
