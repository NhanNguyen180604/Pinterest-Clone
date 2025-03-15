package com.example.pinterest_clone_test2.ui.pin;

import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

public class PinFragmentViewModel extends ViewModel {
    public static String TRANSITION_FINISH_STATE_KEY = "transition_finished_state";
    private final SavedStateHandle _savedStateHandle;

    public PinFragmentViewModel() {
        _savedStateHandle = new SavedStateHandle();
    }

    public boolean getTransitionFinishedState() {
        return Boolean.TRUE.equals(_savedStateHandle.get(TRANSITION_FINISH_STATE_KEY));
    }

    public void setTransitionFinishStateKey(boolean transition_is_finished) {
        _savedStateHandle.set(TRANSITION_FINISH_STATE_KEY, transition_is_finished);
    }
}
