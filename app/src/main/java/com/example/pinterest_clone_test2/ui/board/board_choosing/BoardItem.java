package com.example.pinterest_clone_test2.ui.board.board_choosing;

import com.example.pinterest_clone_test2.models.Board;

public class BoardItem extends BaseItem {
    private final Board board;
    private final boolean isNew;

    public BoardItem(Board board, boolean isNew) {
        this.board = board;
        this.isNew = isNew;
    }

    public Board getBoard() {
        return board;
    }

    @Override
    public int getType() {
        return BoardItemAdapter.VIEW_TYPE_BOARD;
    }

    public boolean isNew() {
        return isNew;
    }
}
