package com.example.pinterest_clone_test2.ui.pin.edit;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import com.example.pinterest_clone_test2.models.Pin;

import java.util.HashMap;
import java.util.Map;

public class EditPinFragmentViewModel extends ViewModel {
    SavedStateHandle savedStateHandle;
    MutableLiveData<Map<String, BoardBooleanPair>> boardMap;
    public static String PIN_STATE_KEY = "PinState";
    public static String IS_AUTHOR_STATE_KEY = "IsAuthorState";

    public EditPinFragmentViewModel(SavedStateHandle savedStateHandle) {
        this.savedStateHandle = savedStateHandle;
        boardMap = new MutableLiveData<>();
        boardMap.setValue(new HashMap<>());
    }

    public void setPinState(Pin pin) {
        savedStateHandle.set(PIN_STATE_KEY, pin);
    }

    public Pin getPinState() {
        return savedStateHandle.get(PIN_STATE_KEY);
    }

    public MutableLiveData<Map<String, BoardBooleanPair>> getBoardMap() {
        return boardMap;
    }

    public boolean getIsAuthor() {
        Boolean isAuthor = savedStateHandle.get(IS_AUTHOR_STATE_KEY);
        return isAuthor != null ? isAuthor : false;
    }

    public void setIsAuthor(boolean isAuthor) {
        savedStateHandle.set(IS_AUTHOR_STATE_KEY, isAuthor);
    }
}
