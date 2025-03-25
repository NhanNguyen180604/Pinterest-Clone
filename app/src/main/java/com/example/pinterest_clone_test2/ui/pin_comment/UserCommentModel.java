package com.example.pinterest_clone_test2.ui.pin_comment;

import android.net.Uri;
import android.view.View;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.databinding.library.baseAdapters.BR;

import com.example.pinterest_clone_test2.models.Comment;

import java.util.Locale;

public class UserCommentModel extends BaseObservable {
    private String _content = "";
    private Uri _attachmentUri;
    private boolean _isFocused;
    private String _replyToId;
    private String _replyToName;

    @Bindable
    public String getContent() {
        return _content;
    }

    public void setContent(String content) {
        _content = content;
        notifyPropertyChanged(BR.content);
        notifyPropertyChanged(BR.postButtonVisibility);
        notifyPropertyChanged(BR.clickable);
    }

    @Bindable
    public String getReplyToId() {
        return _replyToId;
    }

    public void setReplyToId(String replyToId) {
        _replyToId = replyToId;
        notifyPropertyChanged(BR.replyToId);
        notifyPropertyChanged(BR.replyingVisibility);
        notifyPropertyChanged(BR.replyingText);
    }

    @Bindable
    public String getReplyToName() {
        return _replyToName;
    }

    public void setReplyToName(String replyingToName) {
        _replyToName = replyingToName;
        notifyPropertyChanged(BR.replyToName);
        notifyPropertyChanged(BR.replyingVisibility);
        notifyPropertyChanged(BR.replyingText);
    }

    @Bindable
    public Uri getAttachmentUri() {
        return _attachmentUri;
    }

    public void setAttachmentUri(Uri attachmentUri) {
        _attachmentUri = attachmentUri;
        notifyPropertyChanged(BR.attachmentUri);
        notifyPropertyChanged(BR.attachmentVisibility);
        notifyPropertyChanged(BR.clickable);
        notifyPropertyChanged(BR.postButtonVisibility);
    }

    @Bindable
    public boolean getIsFocused() {
        return _isFocused;
    }

    public void setIsFocused(boolean isFocused) {
        _isFocused = isFocused;
        notifyPropertyChanged(BR.isFocused);
        notifyPropertyChanged(BR.postButtonVisibility);
    }

    @Bindable
    public int getAttachmentVisibility() {
        return _attachmentUri != null ? View.VISIBLE : View.GONE;
    }

    @Bindable
    public int getPostButtonVisibility() {
        return (!_content.isEmpty() || _isFocused || _attachmentUri != null) ? View.VISIBLE : View.GONE;
    }

    @Bindable
    public int getReplyingVisibility() {
        return _replyToId != null ? View.VISIBLE : View.GONE;
    }

    @Bindable
    public String getReplyingText() {
        if (_replyToId != null) {
            return String.format(Locale.US, "Replying to %s", _replyToName);
        }
        return "";
    }

    @Bindable
    public boolean getClickable() {
        return !_content.isEmpty() || _attachmentUri != null;
    }

    public Comment createComment() {
        // TODO: put user info here
        return (new Comment())
                .setId("default-id")
                .setAuthorId("default-user-id")
                .setAuthorName("NhanNguyen")
                .setContent(_content)
                .setReplyCommentId(_replyToId)
                .setLikeCount(0)
                .setIsLiked(false)
                .setAttachmentUri(_attachmentUri)
                ;
    }
}
