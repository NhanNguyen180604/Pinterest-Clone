package com.example.pinterest_clone_test2.ui.pin;

import android.os.Parcelable;

import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import com.example.pinterest_clone_test2.models.Pin;
import com.example.pinterest_clone_test2.models.User;

import java.util.List;

public class PinObjectViewModel extends ViewModel {
    private final SavedStateHandle _savedStateHandle;
    public static String SCROLL_STATE_KEY = "scroll_state";
    public static String PIN_STATE_KEY = "pin_state";
    public static String RELEVANT_PIN_STATE_KEY = "relevant_pin_state";
    public static String AUTHOR_STATE_KEY = "author_state";
    public static String SOURCE_STATE_KEY = "source_state";

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

    public String getSource() {
        return _savedStateHandle.get(SOURCE_STATE_KEY);
    }

    public void setSourceState(String new_state) {
        _savedStateHandle.set(SOURCE_STATE_KEY, new_state);
    }

    public User getAuthorState() {
        return _savedStateHandle.get(AUTHOR_STATE_KEY);
    }

    public void setAuthorState(User authorState) {
        _savedStateHandle.set(AUTHOR_STATE_KEY, authorState);
    }

    public List<Pin> getRelevantPinState() {
        return _savedStateHandle.get(RELEVANT_PIN_STATE_KEY);
    }

    public void setRelevantPinState(List<Pin> relevantPinState) {
        _savedStateHandle.set(RELEVANT_PIN_STATE_KEY, relevantPinState);
    }
}
