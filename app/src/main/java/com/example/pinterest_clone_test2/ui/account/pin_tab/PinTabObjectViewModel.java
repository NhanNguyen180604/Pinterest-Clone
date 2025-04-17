package com.example.pinterest_clone_test2.ui.account.pin_tab;

import android.os.Parcelable;

import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import com.example.pinterest_clone_test2.models.Pin;

import java.util.List;

public class PinTabObjectViewModel extends ViewModel {
    private final SavedStateHandle _savedStateHandle;
    public static String SCROLL_STATE = "ScrollState";
    public static String PIN_STATE = "PinState";
    public static String LAST_UPDATE_TIME_STATE = "LastUpdateTimeState";
    public static String PIN_IDS_STATE = "PinIdsState";
    public static String PAGE_STATE = "PageState";

    public PinTabObjectViewModel(SavedStateHandle savedStateHandle) {
        _savedStateHandle = savedStateHandle;
    }

    public Parcelable getScrollState() {
        return _savedStateHandle.get(SCROLL_STATE);
    }

    public void setScrollState(Parcelable scrollState) {
        _savedStateHandle.set(SCROLL_STATE, scrollState);
    }

    public List<Pin> getPinState() {
        return _savedStateHandle.get(PIN_STATE);
    }

    public void setPinState(List<Pin> pinState) {
        _savedStateHandle.set(PIN_STATE, pinState);
    }

    public long getLastUpdateTime() {
        Long result = _savedStateHandle.get(LAST_UPDATE_TIME_STATE);
        return result != null ? result : 0;
    }

    public void setLastUpdateTime(long lastUpdateTime) {
        _savedStateHandle.set(LAST_UPDATE_TIME_STATE, lastUpdateTime);
    }

    public List<String> getPinIdsState() {
        return _savedStateHandle.get(PIN_IDS_STATE);
    }

    public void setPinIdsState(List<String> pinIds) {
        _savedStateHandle.set(PIN_IDS_STATE, pinIds);
    }

    public int getPageState() {
        Integer page = _savedStateHandle.get(PAGE_STATE);
        return page != null ? page : 0;
    }

    public void setPageState(int pageState) {
        _savedStateHandle.set(PAGE_STATE, pageState);
    }
}
