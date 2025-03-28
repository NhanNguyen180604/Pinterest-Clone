package com.example.pinterest_clone_test2.ui.board;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import com.example.pinterest_clone_test2.BR;
import com.example.pinterest_clone_test2.models.Pin;

public class BoardViewModelObservable extends BaseObservable {
    Pin pin;
    String boardName = "";
    boolean isPrivate = false;

    public BoardViewModelObservable() {

    }

    public BoardViewModelObservable(@NonNull Pin pin) {
        setPin(pin);
    }

    @Bindable
    public Pin getPin() {
        return pin;
    }

    public void setPin(@NonNull Pin pin) {
        this.pin = pin;
        notifyPropertyChanged(BR.pin);
        notifyPropertyChanged(BR.previewVisibility);
    }

    @Bindable
    public String getBoardName() {
        return boardName;
    }

    public void setBoardName(String boardName) {
        this.boardName = boardName;
        notifyPropertyChanged(BR.boardName);
        notifyPropertyChanged(BR.canCreate);
    }

    @Bindable
    public int getPreviewVisibility() {
        return pin != null ? View.VISIBLE : View.GONE;
    }

    @Bindable
    public boolean getCanCreate(){
        return !boardName.isBlank();
    }

    @Bindable
    public boolean getIsPrivate() {
        return isPrivate;
    }

    public void setIsPrivate(boolean isPrivate) {
        this.isPrivate = isPrivate;
        notifyPropertyChanged(BR.isPrivate);
    }
}
