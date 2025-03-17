package com.example.pinterest_clone_test2.daos;

import com.example.pinterest_clone_test2.R;
import com.example.pinterest_clone_test2.models.Comment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class MockCommentDao implements ICommentDao {
    List<Comment> _comments = null;

    public MockCommentDao() {
        _comments = Arrays.asList(
                new Comment("comment01", "user01", "Nguyen Thanh Nhan", "lmao", Integer.toString(R.drawable.karyl)),
                new Comment("comment01", "user01", "Nguyen Nhan Thanh", "what the hell"),
                new Comment("comment01", "user01", "Nhan Nguyen Thanh", "requiem", Integer.toString(R.drawable.araragi)),
                new Comment("comment01", "user01", "Nhan Thanh Nguyen", "lorem is my favorite thing of all times, it is so convenient, but i am too lazy to search the google for a lorem so here i am typing this myself god damn it no wonder it takes me forever to code something"),
                new Comment("comment01", "user01", "Thanh Nhan Nguyen", "nani"),
                new Comment("comment01", "user01", "Thanh Nguyen Nhan", "an endless journey, chasing the light, has brought us this way, the myth at last concludes")
        );
    }

    @Override
    public List<Comment> getComments() {
        return _comments;
    }

    @Override
    public List<Comment> getComments(int page, int perPage) {
        if (perPage < 1 || page < 1) {
            throw new IllegalArgumentException("What the **** is wrong with you? How come perPage or page < 1?");
        }

        int fromIndex = (page - 1) * perPage;
        int toIndex = fromIndex + perPage;

        if (_comments == null || _comments.size() < fromIndex) {
            return Collections.emptyList();
        }

        return _comments.subList(fromIndex, Math.min(toIndex, _comments.size()));
    }

    @Override
    public void addComment(Comment newComment) {
        if (newComment == null){
            return;
        }

        // check if duplicate
        if (_comments.stream().anyMatch(comment -> Objects.equals(comment.getId(), newComment.getId()))){
            return;
        };

        _comments.add(newComment);
    }

    @Override
    public void removeComment(Comment comment) {
        _comments.remove(comment);
    }
}
