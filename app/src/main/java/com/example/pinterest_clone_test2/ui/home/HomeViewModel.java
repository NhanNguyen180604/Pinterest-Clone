package com.example.pinterest_clone_test2.ui.home;

import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import com.example.pinterest_clone_test2.models.Board;

import java.util.List;

public class HomeViewModel extends ViewModel {
    private final SavedStateHandle _savedStateHandle;
    public static String BOARD_STATE = "BoardState";

    public HomeViewModel(SavedStateHandle savedStateHandle) {
        _savedStateHandle = savedStateHandle;
    }

    public void setBoardState(List<Board> boardState) {
        _savedStateHandle.set(BOARD_STATE, boardState);
    }

    public List<Board> getBoards() {
        return _savedStateHandle.get(BOARD_STATE);
    }
}