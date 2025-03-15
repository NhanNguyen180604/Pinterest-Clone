package com.example.pinterest_clone_test2.ui.home;

import android.os.Parcelable;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class TabObjectViewModel extends ViewModel {
    private final MutableLiveData<Parcelable> _scrollState;

    public TabObjectViewModel() {
        _scrollState = new MutableLiveData<>();
    }

    public Parcelable getScrollState() {
        return _scrollState.getValue();
    }

    public void setScrollState(Parcelable new_state) {
        this._scrollState.setValue(new_state);
    }
}
