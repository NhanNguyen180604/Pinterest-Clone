package com.example.pinterest_clone_test2.ui.pin_comment;

import android.net.Uri;
import android.view.View;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.databinding.library.baseAdapters.BR;

public class UserCommentModel extends BaseObservable {
    private String _content;
    private Uri _attachmentUri;

    @Bindable
    public String getContent() {
        return _content;
    }

    public void setContent(String content) {
        _content = content;
        notifyPropertyChanged(BR.content);
    }

    @Bindable
    public Uri getAttachmentUri() {
        return _attachmentUri;
    }

    public void setAttachmentUri(Uri attachmentUri) {
        _attachmentUri = attachmentUri;
        notifyPropertyChanged(BR.attachmentUri);
    }

    public int getAttachmentVisibility() {
        return _attachmentUri != null ? View.VISIBLE : View.GONE;
    }
}
