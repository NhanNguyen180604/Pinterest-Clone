package com.example.pinterest_clone_test2.daos;

import androidx.annotation.NonNull;

import com.example.pinterest_clone_test2.models.Comment;

import java.util.List;

public interface ICommentDao {
    List<Comment> getComments();
    List<Comment> getComments(int page, int perPage);
    void addComment(@NonNull Comment newComment);
    void removeComment(@NonNull Comment comment);
}
