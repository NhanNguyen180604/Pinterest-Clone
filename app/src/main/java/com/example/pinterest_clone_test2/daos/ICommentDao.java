package com.example.pinterest_clone_test2.daos;

import com.example.pinterest_clone_test2.models.Comment;

import java.util.List;

public interface ICommentDao {
    List<Comment> getComments();
    List<Comment> getComments(int page, int perPage);
    void addComment(Comment newComment);
    void removeComment(Comment comment);
}
