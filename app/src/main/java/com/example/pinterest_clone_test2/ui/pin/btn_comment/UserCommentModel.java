package com.example.pinterest_clone_test2.ui.pin.btn_comment;

import android.content.Context;
import android.net.Uri;
import android.view.View;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.databinding.library.baseAdapters.BR;

import com.example.pinterest_clone_test2.R;
import com.example.pinterest_clone_test2.models.Comment;

import java.util.Locale;

public class UserCommentModel extends BaseObservable {
    private String _content = "";
    private Uri _attachmentUri;
    private boolean _isFocused;
    private String _replyToId;
    private String _replyToName;
    private final Context _context;

    public UserCommentModel(Context context) {
        _context = context;
    }

    @Bindable
    public String getContent() {
        return _content;
    }

    public void setContent(String content) {
        _content = content;
        notifyPropertyChanged(BR.content);
        notifyPropertyChanged(BR.postButtonVisibility);
        notifyPropertyChanged(BR.enabled);
        notifyPropertyChanged(BR.backgroundTint);
        notifyPropertyChanged(BR.tint);
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
        notifyPropertyChanged(BR.enabled);
        notifyPropertyChanged(BR.backgroundTint);
        notifyPropertyChanged(BR.tint);
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
            return String.format(Locale.US, _context.getResources().getString(R.string.replying_to), _replyToName);
        }
        return "";
    }

    @Bindable
    public boolean getEnabled() {
        return !_content.isEmpty() || _attachmentUri != null;
    }

    @Bindable
    public int getBackgroundTint() {
        return getEnabled() ? _context.getColor(R.color.red_pinterest) : _context.getColor(R.color.grey_hint);
    }

    @Bindable
    public int getTint() {
        return getEnabled() ? _context.getColor(R.color.white) : _context.getColor(R.color.dark_grey);
    }

    public Comment createComment() {
        // TODO: put user info here
        return (new Comment(_context))
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
