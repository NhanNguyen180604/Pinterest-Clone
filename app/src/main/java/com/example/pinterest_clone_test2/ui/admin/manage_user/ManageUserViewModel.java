package com.example.pinterest_clone_test2.ui.admin.manage_user;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ManageUserViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public ManageUserViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is home fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}