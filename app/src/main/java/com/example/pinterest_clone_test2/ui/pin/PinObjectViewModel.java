package com.example.pinterest_clone_test2.ui.pin;

import android.os.Parcelable;

import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import com.example.pinterest_clone_test2.models.Pin;

public class PinObjectViewModel extends ViewModel {
    private final SavedStateHandle _savedStateHandle;
    public static String SCROLL_STATE_KEY = "scroll_state";
    public static String PIN_STATE_KEY = "pin_state";
    public static String INITIAL_STATE_KEY = "initial_state";
    public static String TRANSITION_FINISHED_STATE = "transition_finished_state";

    public PinObjectViewModel(SavedStateHandle savedStateHandle) {
        _savedStateHandle = savedStateHandle;
        if (!_savedStateHandle.contains(INITIAL_STATE_KEY)) {
            _savedStateHandle.set(INITIAL_STATE_KEY, -1);
        }
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

    public int getInitialState() {
        Integer state = _savedStateHandle.get(INITIAL_STATE_KEY);
        return state != null ? state : -1;
    }

    public void setInitialState(int is_initial_state) {
        _savedStateHandle.set(INITIAL_STATE_KEY, is_initial_state);
    }

    public boolean getTransitionFinishedState() {
        return Boolean.TRUE.equals(_savedStateHandle.get(TRANSITION_FINISHED_STATE));
    }

    public void setTransitionFinishedState(boolean transition_finished_state) {
        _savedStateHandle.set(TRANSITION_FINISHED_STATE, transition_finished_state);
    }
}
