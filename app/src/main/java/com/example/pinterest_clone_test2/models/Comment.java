package com.example.pinterest_clone_test2.models;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.databinding.library.baseAdapters.BR;

import com.example.pinterest_clone_test2.R;

import java.util.Locale;

public class Comment extends BaseObservable {
    String _id;

    // will replace with User model if necessary
    String _authorId;
    String _authorName;

    String _content;
    String _attachmentUrl = null;
    String _replyTo = null;
    int _likeCount;

    // will replace with UserCommentLike model if necessary
    String _userLikeId;
    boolean _isLiked = false;

    @Bindable
    public String getId() {
        return _id;
    }

    @Bindable
    public String getAuthorId() {
        return _authorId;
    }

    @Bindable
    public String getAuthorName() {
        return _authorName;
    }

    @Bindable
    public String getContent() {
        return _content;
    }

    @Bindable
    public String getAttachmentUrl() {
        return _attachmentUrl;
    }

    @Bindable
    public String getReplyCommentId() {
        return _replyTo;
    }

    @Bindable
    public int getLikeCount() {
        return _likeCount;
    }

    @Bindable
    public boolean getIsLiked() {
        return _isLiked;
    }

    @Bindable
    public String getUserLikeId() {
        return _userLikeId;
    }

    // replicating builder pattern for better mock data initialization
    public Comment setId(String id) {
        _id = id;
        notifyPropertyChanged(BR.id);
        return this;
    }

    public Comment setAuthorId(String authorId) {
        _authorId = authorId;
        notifyPropertyChanged(BR.authorId);
        return this;
    }

    public Comment setAuthorName(String authorName) {
        _authorName = authorName;
        notifyPropertyChanged(BR.authorName);
        return this;
    }

    public Comment setContent(String content) {
        _content = content;
        notifyPropertyChanged(BR.content);
        return this;
    }

    public Comment setAttachmentUrl(String attachmentUrl) {
        _attachmentUrl = attachmentUrl;
        notifyPropertyChanged(BR.attachmentUrl);
        return this;
    }

    public Comment setReplyCommentId(String replyTo) {
        _replyTo = replyTo;
        notifyPropertyChanged(BR.replyCommentId);
        return this;
    }

    public Comment setLikeCount(int likeCount) {
        _likeCount = likeCount;
        notifyPropertyChanged(BR.likeCount);
        notifyPropertyChanged(BR.reactionString);
        return this;
    }

    public Comment setIsLiked(boolean isLiked) {
        _isLiked = isLiked;
        notifyPropertyChanged(BR.isLiked);
        notifyPropertyChanged(BR.reactionIcon);
        return this;
    }

    public Comment setUserLikeId(String userLikeId) {
        _userLikeId = userLikeId;
        notifyPropertyChanged(BR.userLikeId);
        return this;
    }

    @Bindable
    public String getReactionString() {
        return String.format(Locale.US, "%d reaction%s", _likeCount, _likeCount > 1 ? "s" : "");
    }

    @Bindable
    public int getReactionIcon() {
        return _isLiked ? R.drawable.ic_favorite_heart_filled : R.drawable.ic_favorite_heart;
    }
}

