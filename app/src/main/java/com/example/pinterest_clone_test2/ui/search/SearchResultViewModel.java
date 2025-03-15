package com.example.pinterest_clone_test2.ui.search;

import android.os.Parcelable;

import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

public class SearchResultViewModel extends ViewModel {
    SavedStateHandle _savedStateHandle;
    public static String SCROLL_STATE_KEY = "scroll_state";
    public SearchResultViewModel(SavedStateHandle savedStateHandle) {
        _savedStateHandle = savedStateHandle;
    }

    public Parcelable getScrollState() {
        return _savedStateHandle.get(SCROLL_STATE_KEY);
    }

    public void setScrollState(Parcelable new_scroll_state) {
        _savedStateHandle.set(SCROLL_STATE_KEY, new_scroll_state);
    }
}
