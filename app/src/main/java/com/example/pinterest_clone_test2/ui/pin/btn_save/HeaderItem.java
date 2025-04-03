package com.example.pinterest_clone_test2.ui.pin.btn_save;

public class HeaderItem extends BaseItem {
    private final String title;

    public HeaderItem(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public int getType() {
        return BoardItemAdapter.VIEW_TYPE_HEADER;
    }
}
