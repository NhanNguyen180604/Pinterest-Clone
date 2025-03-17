package com.example.pinterest_clone_test2.models;

public class Comment {
    String _id;
    // will replace with User model
    String _authorId;
    String _authorName;
    String _content;
    String _attachmentUrl = null;
    String _replyTo = null;

    public String getId() {
        return _id;
    }

    public String getAuthorId() {
        return _authorId;
    }

    public String getAuthorName() {
        return _authorName;
    }

    public String getContent() {
        return _content;
    }

    public String getAttachmentUrl() {
        return _attachmentUrl;
    }

    public String getReplyCommentId() {
        return _replyTo;
    }

    public Comment setId(String id) {
        _id = id;
        return this;
    }

    public Comment setAuthorId(String authorId) {
        _authorId = authorId;
        return this;
    }

    public Comment setAuthorName(String authorName) {
        _authorName = authorName;
        return this;
    }

    public Comment setContent(String content) {
        _content = content;
        return this;
    }

    public Comment setAttachmentUrl(String attachmentUrl) {
        _attachmentUrl = attachmentUrl;
        return this;
    }

    public Comment setReplyCommentId(String replyTo) {
        _replyTo = replyTo;
        return this;
    }
}

