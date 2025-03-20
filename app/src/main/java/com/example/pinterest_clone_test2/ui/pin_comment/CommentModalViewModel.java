package com.example.pinterest_clone_test2.ui.pin_comment;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.pinterest_clone_test2.models.Comment;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CommentModalViewModel extends ViewModel {
    private final MutableLiveData<List<Comment>> _comments;
    private final UserCommentModel _userCommentModel;

    public CommentModalViewModel() {
        _comments = new MutableLiveData<>();
        _comments.setValue(new ArrayList<>());
        _userCommentModel = new UserCommentModel();
    }

    public MutableLiveData<List<Comment>> getComments() {
        return _comments;
    }

    public void addComments(List<Comment> newComments) {
        List<Comment> comments = _comments.getValue();
        assert comments != null;

        if (newComments == null) {
            comments.add(null);
        } else {
            comments.addAll(newComments);
        }

        _comments.setValue(comments);
    }

    public void removeLastComment() {
        List<Comment> comments = _comments.getValue();
        assert comments != null;
        comments.remove(comments.size() - 1);
        _comments.setValue(comments);
    }

    public String getCommentCountString() {
        int count = _comments.getValue() != null ? _comments.getValue().size() : 0;
        String result = String.format(Locale.US, "Now showing %d comment", count);
        return result + ((count > 1) ? "s" : "");
    }

    public UserCommentModel getUserCommentModel() {
        return _userCommentModel;
    }
}
