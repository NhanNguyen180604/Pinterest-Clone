package com.example.pinterest_clone_test2.daos;

import androidx.annotation.NonNull;

import com.example.pinterest_clone_test2.models.Comment;

import java.util.ArrayList;
import java.util.Arrays;
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
                        .setUserLikeId("default-user-id")
                        .setIsLiked(true)
                        .setAttachmentUrl("https://res.cloudinary.com/dstlbw3xa/image/upload/c_thumb,w_200,g_face/v1741956521/67684ec351fca61bd69f7716/2/ufmcj7or3tp6wheumsfs.jpg"),
                new Comment()
                        .setId("comment02")
                        .setAuthorId("user02")
                        .setAuthorName("Nguyen Nhan Thanh")
                        .setContent("")
                        .setLikeCount(10)
                        .setReplyCommentId("comment01")
                        .setUserLikeId("default-user-id")
                        .setIsLiked(false)
                        .setAttachmentUrl("https://res.cloudinary.com/dstlbw3xa/image/upload/c_thumb,w_200,g_face/v1741956525/67684ec351fca61bd69f7716/2/s8oo7ktm2gf1wvawqg3n.png"),
                new Comment()
                        .setId("comment03")
                        .setAuthorId("user03")
                        .setAuthorName("Nhan Nguyen Thanh")
                        .setContent("requiem")
                        .setLikeCount(12)
                        .setUserLikeId("default-user-id")
                        .setIsLiked(false)
                        .setAttachmentUrl("https://res.cloudinary.com/dstlbw3xa/image/upload/c_thumb,w_200,g_face/v1735305519/676ea8facd4b26dc7654c093/1/b0pqu9vtrriylx80qqol.png"),
                new Comment()
                        .setId("comment04")
                        .setAuthorId("user04")
                        .setAuthorName("Nhan Thanh Nguyen")
                        .setLikeCount(16)
                        .setUserLikeId("default-user-id")
                        .setIsLiked(true)
                        .setContent("lorem is my favorite thing of all times, it is so convenient, but i am too lazy to search the google for a lorem so here i am typing this myself god damn it no wonder it takes me forever to code something"),
                new Comment()
                        .setId("comment05")
                        .setAuthorId("user05")
                        .setAuthorName("Thanh Nhan Nguyen")
                        .setContent("nani")
                        .setReplyCommentId("comment04")
                        .setLikeCount(69)
                        .setAttachmentUrl("https://res.cloudinary.com/dstlbw3xa/image/upload/c_thumb,w_200,g_face/v1735307406/675e9bcb4231a81f56b82c11/4/pux9egxwo0upxczkti8b.png"),
                new Comment()
                        .setId("comment06")
                        .setAuthorId("user06")
                        .setAuthorName("Thanh Nguyen Nhan")
                        .setLikeCount(1234)
                        .setUserLikeId("default-user-id")
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

    // Do not use this
    @Override
    public List<Comment> getComments(int page, int perPage) {
        return new ArrayList<>();
    }

    @Override
    public void addComment(@NonNull Comment newComment) {
        // check if duplicate
        if (_comments.stream().anyMatch(comment -> Objects.equals(comment.getId(), newComment.getId()))) {
            return;
        }

        _comments.add(newComment);
    }

    @Override
    public void removeComment(@NonNull Comment comment) {
        _comments.remove(comment);
    }
}
