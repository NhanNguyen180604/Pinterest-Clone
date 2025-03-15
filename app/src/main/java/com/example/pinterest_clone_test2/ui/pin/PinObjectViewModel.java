package com.example.pinterest_clone_test2.ui.pin;

import android.os.Parcelable;

import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import com.example.pinterest_clone_test2.models.Pin;

public class PinObjectViewModel extends ViewModel {
    private final SavedStateHandle _savedStateHandle;
    public static String SCROLL_STATE_KEY = "scroll_state";
    public static String PIN_STATE_KEY = "pin_state";

    public PinObjectViewModel(SavedStateHandle savedStateHandle) {
        _savedStateHandle = savedStateHandle;
    }

    public Parcelable getScrollState() {
        return _savedStateHandle.get(SCROLL_STATE_KEY);
    }

    public void setScrollState(Parcelable new_state) {
        _savedStateHandle.set(SCROLL_STATE_KEY, new_state);
    }

    public Pin getPinState() {
        return _savedStateHandle.get(PIN_STATE_KEY);
    }

    public void setPinState(Pin pin_state) {
        _savedStateHandle.set(PIN_STATE_KEY, pin_state);
    }
}
