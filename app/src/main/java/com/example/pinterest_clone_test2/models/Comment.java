package com.example.pinterest_clone_test2.models;

import androidx.annotation.Nullable;

public class Comment {
    String _id;
    // will replace with User model
    String _authorId;
    String _authorName;
    String _content;
    String _attachmentUrl = null;
    String _replyTo = null;

    public Comment(String id, String authorId, String authorName, String content) {
        _id = id;
        _authorId = authorId;
        _authorName = authorName;
        _content = content;
    }

    public Comment(String id, String authorId, String authorName, String content, String attachmentUrl) {
        _id = id;
        _authorId = authorId;
        _authorName = authorName;
        _content = content;
        _attachmentUrl = attachmentUrl;
    }

    public String getId() {
        return _id;
    }

    public String getAuthorId() {
        return _authorId;
    }

    public String getAuthorName() {
        return _authorName;
    }

    @Nullable
    public String getAttachmentUrl() {
        return _attachmentUrl;
    }

    public String getContent() {
        return _content;
    }

    @Nullable
    public String getReplyCommentId() {
        return _replyTo;
    }
}

