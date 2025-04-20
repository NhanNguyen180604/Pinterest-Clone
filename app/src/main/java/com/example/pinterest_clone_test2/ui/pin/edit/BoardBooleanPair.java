package com.example.pinterest_clone_test2.ui.pin.edit;

import com.example.pinterest_clone_test2.models.Board;

public class BoardBooleanPair {
    Board board;
    boolean included;

    public Board getBoard() {
        return board;
    }

    public BoardBooleanPair setBoard(Board board) {
        this.board = board;
        return this;
    }

    public boolean isIncluded() {
        return included;
    }

    public BoardBooleanPair setIncluded(boolean included) {
        this.included = included;
        return this;
    }
}
