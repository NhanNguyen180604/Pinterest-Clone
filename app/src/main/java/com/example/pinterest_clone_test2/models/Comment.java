package com.example.pinterest_clone_test2.models;

import android.content.Context;
import android.net.Uri;
import android.view.View;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.databinding.library.baseAdapters.BR;

import com.example.pinterest_clone_test2.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Locale;
import java.util.Objects;

public class Comment extends BaseObservable {
    String _id;
    String _pinId;

    // will replace with User model if necessary
    String _authorId;
    String _authorName;
    String _authorAvatarUrl;

    String _content;
    String _attachmentUrl = null;
    String _attachmentThumbnailUrl = null;
    String _replyTo = null;
    int _likeCount;
    long _createdAt;
    private final Context _context;

    // will replace with UserCommentLike model if necessary
    String _userLikeId;
    boolean _isLiked = false;
    Uri _attachmentUri;  // for uploading comment

    public Comment(Context context) {
        _context = context;
    }

    public String getId() {
        return _id;
    }

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

    @Bindable
    public Uri getAttachmentUri() {
        return _attachmentUri;
    }

    @Bindable
    public String getAttachmentThumbnailUrl() {
        return _attachmentThumbnailUrl;
    }

    @Bindable
    public String getAuthorAvatarUrl() {
        return _authorAvatarUrl;
    }

    public String getPinId() {
        return _pinId;
    }

    @Bindable
    public long getCreatedAt() {
        return _createdAt;
    }

    // replicating builder pattern for better mock data initialization
    public Comment setId(String id) {
        _id = id;
        return this;
    }

    public Comment setAuthorId(String authorId) {
        _authorId = authorId;
        notifyPropertyChanged(BR.optionsVisibility);
        return this;
    }

    public Comment setAuthorName(String authorName) {
        _authorName = authorName;
        notifyPropertyChanged(BR.authorName);
        return this;
    }

    public Comment setAuthorAvatarUrl(String authorAvatarUrl) {
        _authorAvatarUrl = authorAvatarUrl;
        notifyPropertyChanged(BR.authorAvatarUrl);
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
        notifyPropertyChanged(BR.attachmentVisibility);
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

    public Comment setAttachmentUri(Uri attachmentUri) {
        _attachmentUri = attachmentUri;
        notifyPropertyChanged(BR.attachmentUri);
        notifyPropertyChanged(BR.attachmentVisibility);
        return this;
    }

    public Comment setAttachmentThumbnailUrl(String attachmentThumbnailUrl) {
        _attachmentThumbnailUrl = attachmentThumbnailUrl;
        notifyPropertyChanged(BR.attachmentThumbnailUrl);
        return this;
    }

    public Comment setPinId(String pinId) {
        _pinId = pinId;
        return this;
    }

    public Comment setCreatedAt(long createdAt) {
        _createdAt = createdAt;
        notifyPropertyChanged(BR.createdAt);
        return this;
    }

    // could have refactor this, but ran out of time
    // too bad
    @Bindable
    public String getReactionString() {
        return String.format(Locale.US, _context.getResources().getString(R.string.reaction_count_string), _likeCount);
    }

    @Bindable
    public int getReactionIcon() {
        return _isLiked ? R.drawable.ic_favorite_heart_filled : R.drawable.ic_favorite_heart;
    }

    @Bindable
    public int getOptionsVisibility() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        assert currentUser != null;
        return Objects.equals(_authorId, currentUser.getUid()) ? View.GONE : View.VISIBLE;
    }

    @Bindable
    public int getAttachmentVisibility() {
        return _attachmentUrl != null || _attachmentUri != null ? View.VISIBLE : View.GONE;
    }

    public boolean isValidComment() {
        return _content != null && _authorId != null && _pinId != null;
    }
}

