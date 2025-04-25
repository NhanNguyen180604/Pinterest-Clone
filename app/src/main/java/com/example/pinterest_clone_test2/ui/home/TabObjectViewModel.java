package com.example.pinterest_clone_test2.ui.home;

import android.os.Parcelable;

import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import com.example.pinterest_clone_test2.models.Board;
import com.example.pinterest_clone_test2.models.Pin;

import java.util.List;

public class TabObjectViewModel extends ViewModel {
    private final SavedStateHandle _savedStateHandle;
    public static String SCROLL_STATE = "ScrollState";
    public static String PIN_STATE = "PinState";
    public static String LAST_PAGE_STATE = "OnLastPageState";
    public static String BOARD_STATE = "BoardState";

    public TabObjectViewModel(SavedStateHandle savedStateHandle) {
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

    public boolean isOnLastPage() {
        return Boolean.TRUE.equals(_savedStateHandle.get(LAST_PAGE_STATE));
    }

    public void setOnLastPage(boolean isOnLastPage) {
        _savedStateHandle.set(LAST_PAGE_STATE, isOnLastPage);
    }

    public Board getBoard() {
        return _savedStateHandle.get(BOARD_STATE);
    }

    public void setBoardState(Board board) {
        _savedStateHandle.set(BOARD_STATE, board);
    }
}
