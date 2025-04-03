package com.example.pinterest_clone_test2.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Pin implements Parcelable {
    public enum PinType {
        IMAGE,
        GIF,
        VIDEO
    }

    private String id;
    private String mediaUrl;
    private String thumbnailUrl;  // for image
    private String authorId;
    PinType type;
    boolean isLiked;
    int likeCount;
    private String name;
    private String description;
    private boolean allowComment;
    private long createdAt;

    public Pin() {

    }

    public String getId() {
        return id;
    }

    public Pin setId(String id) {
        this.id = id;
        return this;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public Pin setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
        return this;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public Pin setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
        return this;
    }

    public String getAuthorId() {
        return authorId;
    }

    public Pin setAuthorId(String authorId) {
        this.authorId = authorId;
        return this;
    }

    public PinType getType() {
        return type;
    }

    public Pin setType(PinType type) {
        this.type = type;
        return this;
    }

    public boolean getIsLiked() {
        return isLiked;
    }

    public Pin setIsLiked(boolean liked) {
        isLiked = liked;
        return this;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public Pin setLikeCount(int likeCount) {
        this.likeCount = likeCount;
        return this;
    }

    public String getName() {
        return name;
    }

    public Pin setName(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public Pin setDescription(String description) {
        this.description = description;
        return this;
    }

    public boolean getAllowComment() {
        return allowComment;
    }

    public Pin setAllowComment(boolean allowComment) {
        this.allowComment = allowComment;
        return this;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public Pin setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
        return this;
    }
    public static List<Pin> testData = new ArrayList<>(Arrays.asList(
            new Pin()
                    .setId("pin01")
                    .setAuthorId("user01")
                    .setType(Pin.PinType.IMAGE)
                    .setMediaUrl("https://res.cloudinary.com/dstlbw3xa/image/upload/v1741956525/67684ec351fca61bd69f7716/2/s8oo7ktm2gf1wvawqg3n.png")
                    .setThumbnailUrl("https://res.cloudinary.com/dstlbw3xa/image/upload/c_thumb,w_200,g_face/v1741956525/67684ec351fca61bd69f7716/2/s8oo7ktm2gf1wvawqg3n.png")
                    .setIsLiked(true)
                    .setLikeCount(1500),
            new Pin()
                    .setId("pin02")
                    .setAuthorId("user02")
                    .setType(Pin.PinType.IMAGE)
                    .setMediaUrl("https://res.cloudinary.com/dstlbw3xa/image/upload/v1741956521/67684ec351fca61bd69f7716/2/ufmcj7or3tp6wheumsfs.jpg")
                    .setThumbnailUrl("https://res.cloudinary.com/dstlbw3xa/image/upload/c_thumb,w_200,g_face/v1741956521/67684ec351fca61bd69f7716/2/ufmcj7or3tp6wheumsfs.jpg")
                    .setIsLiked(true)
                    .setLikeCount(1469),
            new Pin()
                    .setId("pin03")
                    .setAuthorId("user03")
                    .setType(Pin.PinType.IMAGE)
                    .setMediaUrl("https://res.cloudinary.com/dstlbw3xa/image/upload/v1735375550/675e9bcb4231a81f56b82c11/6/toz3hcn86mqcsins2gsx.png")
                    .setThumbnailUrl("https://res.cloudinary.com/dstlbw3xa/image/upload/c_thumb,w_200,g_face/v1735375550/675e9bcb4231a81f56b82c11/6/toz3hcn86mqcsins2gsx.png")
                    .setIsLiked(false)
                    .setLikeCount(432),
            new Pin()
                    .setId("pin04")
                    .setAuthorId("user04")
                    .setType(Pin.PinType.IMAGE)
                    .setMediaUrl("https://res.cloudinary.com/dstlbw3xa/image/upload/v1735304610/676ea599cd4b26dc7654ba09/cover/d6euwabyejfnqyslk3cl.png")
                    .setThumbnailUrl("https://res.cloudinary.com/dstlbw3xa/image/upload/c_thumb,w_200,g_face/v1735304610/676ea599cd4b26dc7654ba09/cover/d6euwabyejfnqyslk3cl.png")
                    .setIsLiked(true)
                    .setLikeCount(123),
            new Pin()
                    .setId("pin05")
                    .setAuthorId("user05")
                    .setType(Pin.PinType.IMAGE)
                    .setMediaUrl("https://res.cloudinary.com/dstlbw3xa/image/upload/v1735305519/676ea8facd4b26dc7654c093/1/b0pqu9vtrriylx80qqol.png")
                    .setThumbnailUrl("https://res.cloudinary.com/dstlbw3xa/image/upload/c_thumb,w_200,g_face/v1735305519/676ea8facd4b26dc7654c093/1/b0pqu9vtrriylx80qqol.png")
                    .setIsLiked(true)
                    .setLikeCount(1456),
            new Pin()
                    .setId("pin06")
                    .setAuthorId("user06")
                    .setType(Pin.PinType.IMAGE)
                    .setMediaUrl("https://res.cloudinary.com/dstlbw3xa/image/upload/v1735306508/676eab58cd4b26dc7654c1ca/1/fg1obhmbeixukqrpsvpf.png")
                    .setThumbnailUrl("https://res.cloudinary.com/dstlbw3xa/image/upload/c_thumb,w_200,g_face/v1735306508/676eab58cd4b26dc7654c1ca/1/fg1obhmbeixukqrpsvpf.png")
                    .setIsLiked(false)
                    .setLikeCount(600),
            new Pin()
                    .setId("pin07")
                    .setAuthorId("user07")
                    .setType(Pin.PinType.IMAGE)
                    .setMediaUrl("https://res.cloudinary.com/dstlbw3xa/image/upload/v1735306081/676eab58cd4b26dc7654c1ca/cover/myrcpzkfatskgrc49y6f.png")
                    .setThumbnailUrl("https://res.cloudinary.com/dstlbw3xa/image/upload/c_thumb,w_200,g_face/v1735306081/676eab58cd4b26dc7654c1ca/cover/myrcpzkfatskgrc49y6f.png")
                    .setIsLiked(false)
                    .setLikeCount(368),
            new Pin()
                    .setId("pin08")
                    .setAuthorId("user08")
                    .setType(Pin.PinType.IMAGE)
                    .setMediaUrl("https://res.cloudinary.com/dstlbw3xa/image/upload/v1735305097/676ea65bcd4b26dc7654bc0a/1/ascdenxcybbktydym0qf.png")
                    .setThumbnailUrl("https://res.cloudinary.com/dstlbw3xa/image/upload/c_thumb,w_200,g_face/v1735305097/676ea65bcd4b26dc7654bc0a/1/ascdenxcybbktydym0qf.png")
                    .setIsLiked(false)
                    .setLikeCount(160),
            new Pin()
                    .setId("pin09")
                    .setAuthorId("user09")
                    .setType(Pin.PinType.IMAGE)
                    .setMediaUrl("https://res.cloudinary.com/dstlbw3xa/image/upload/v1735121033/67684e7751fca61bd69f762f/cover/ttp3u0rkx4nqzugwjjqw.png")
                    .setThumbnailUrl("https://res.cloudinary.com/dstlbw3xa/image/upload/c_thumb,w_200,g_face/v1735121033/67684e7751fca61bd69f762f/cover/ttp3u0rkx4nqzugwjjqw.png")
                    .setIsLiked(true)
                    .setLikeCount(1291),
            new Pin()
                    .setId("pin10")
                    .setAuthorId("user10")
                    .setType(Pin.PinType.IMAGE)
                    .setMediaUrl("https://res.cloudinary.com/dstlbw3xa/image/upload/v1734364422/avatar/6706517c0b92f958b833e64c/xz66lv3pdvaaektnouuy.png")
                    .setThumbnailUrl("https://res.cloudinary.com/dstlbw3xa/image/upload/c_thumb,w_200,g_face/v1734364422/avatar/6706517c0b92f958b833e64c/xz66lv3pdvaaektnouuy.png")
                    .setIsLiked(false)
                    .setLikeCount(591),
            new Pin()
                    .setId("pin11")
                    .setAuthorId("user11")
                    .setType(Pin.PinType.IMAGE)
                    .setMediaUrl("https://res.cloudinary.com/dstlbw3xa/image/upload/v1735305474/676ea8facd4b26dc7654c093/cover/w2zfkqxrfyvewyh6jzhn.png")
                    .setThumbnailUrl("https://res.cloudinary.com/dstlbw3xa/image/upload/c_thumb,w_200,g_face/v1735305474/676ea8facd4b26dc7654c093/cover/w2zfkqxrfyvewyh6jzhn.png")
                    .setIsLiked(true)
                    .setLikeCount(1329),
            new Pin()
                    .setId("pin12")
                    .setAuthorId("user12")
                    .setType(Pin.PinType.IMAGE)
                    .setMediaUrl("https://res.cloudinary.com/dstlbw3xa/image/upload/v1735304864/676ea599cd4b26dc7654ba09/1/bj9lp4afe5v6ufb4lrcf.png")
                    .setThumbnailUrl("https://res.cloudinary.com/dstlbw3xa/image/upload/c_thumb,w_200,g_face/v1735304864/676ea599cd4b26dc7654ba09/1/bj9lp4afe5v6ufb4lrcf.png")
                    .setIsLiked(true)
                    .setLikeCount(1425)
    ));

    public Pin(Parcel in) {
        super();
        readFromParcel(in);
    }

    public static final Parcelable.Creator<Pin> CREATOR = new Parcelable.Creator<>() {

        @Override
        public Pin createFromParcel(Parcel source) {
            return new Pin(source);
        }

        @Override
        public Pin[] newArray(int size) {
            return new Pin[size];
        }
    };

    public void readFromParcel(Parcel in) {
        id = in.readString();
        mediaUrl = in.readString();
        authorId = in.readString();
        thumbnailUrl = in.readString();
        type = (PinType) in.readSerializable();
        isLiked = in.readBoolean();
        likeCount = in.readInt();
        name = in.readString();
        description = in.readString();
        allowComment = in.readByte() != 0;
        createdAt = in.readLong();
    }
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(mediaUrl);
        dest.writeString(authorId);
        dest.writeString(thumbnailUrl);
        dest.writeSerializable(type);
        dest.writeBoolean(isLiked);
        dest.writeInt(likeCount);
        dest.writeString(name);
        dest.writeString(description);
        dest.writeByte((byte) (allowComment ? 1 : 0));
        dest.writeLong(createdAt);
    }
}
