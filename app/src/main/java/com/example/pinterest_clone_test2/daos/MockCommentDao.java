package com.example.pinterest_clone_test2.daos;

import com.example.pinterest_clone_test2.R;
import com.example.pinterest_clone_test2.models.Comment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class MockCommentDao implements ICommentDao {
    List<Comment> _comments;

    // the backend should sort the comments such that reply always follow the original comment
    // or else im gonna kms
    public MockCommentDao() {
        _comments = new ArrayList<>(Arrays.asList(
                new Comment()
                        .setId("comment01")
                        .setAuthorId("user01")
                        .setAuthorName("Nguyen Thanh Nhan")
                        .setContent("lmao")
                        .setLikeCount(1)
                        .setUserLikeId("user69")
                        .setIsLiked(true)
                        .setAttachmentUrl(Integer.toString(R.drawable.karyl)),
                new Comment()
                        .setId("comment02")
                        .setAuthorId("user02")
                        .setAuthorName("Nguyen Nhan Thanh")
                        .setContent("")
                        .setLikeCount(10)
                        .setReplyCommentId("comment01")
                        .setUserLikeId("user69")
                        .setIsLiked(false)
                        .setAttachmentUrl(Integer.toString(R.drawable.turtle_huh)),
                new Comment()
                        .setId("comment03")
                        .setAuthorId("user03")
                        .setAuthorName("Nhan Nguyen Thanh")
                        .setContent("requiem")
                        .setLikeCount(12)
                        .setUserLikeId("user69")
                        .setIsLiked(false)
                        .setAttachmentUrl(Integer.toString(R.drawable.araragi)),
                new Comment()
                        .setId("comment04")
                        .setAuthorId("user04")
                        .setAuthorName("Nhan Thanh Nguyen")
                        .setLikeCount(16)
                        .setUserLikeId("user69")
                        .setIsLiked(true)
                        .setContent("lorem is my favorite thing of all times, it is so convenient, but i am too lazy to search the google for a lorem so here i am typing this myself god damn it no wonder it takes me forever to code something"),
                new Comment()
                        .setId("comment05")
                        .setAuthorId("user05")
                        .setAuthorName("Thanh Nhan Nguyen")
                        .setContent("nani")
                        .setReplyCommentId("comment04")
                        .setLikeCount(69)
                        .setAttachmentUrl(Integer.toString(R.drawable.turtle_huh)),
                new Comment()
                        .setId("comment06")
                        .setAuthorId("user06")
                        .setAuthorName("Thanh Nguyen Nhan")
                        .setLikeCount(1234)
                        .setUserLikeId("user69")
                        .setIsLiked(true)
                        .setContent("An endless journey.\n" +
                                "Chasing the light, brought us all this way.\n" +
                                "The myth at last concludes.\n" +
                                "Let us close our eyes, just as we've been told.\n" +
                                "And let us dream, new dreams.")
        ));
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
        if (newComment == null) {
            return;
        }

        // check if duplicate
        if (_comments.stream().anyMatch(comment -> Objects.equals(comment.getId(), newComment.getId()))) {
            return;
        }

        _comments.add(newComment);
    }

    @Override
    public void removeComment(Comment comment) {
        _comments.remove(comment);
    }
}
