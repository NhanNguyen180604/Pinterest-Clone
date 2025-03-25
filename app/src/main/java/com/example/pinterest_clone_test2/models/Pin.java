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

    public static List<Pin> testData = new ArrayList<>(Arrays.asList(
            new Pin()
                    .setId("pin01")
                    .setAuthorId("user01")
                    .setType(Pin.PinType.IMAGE)
                    .setMediaUrl("https://res.cloudinary.com/dstlbw3xa/image/upload/v1741956525/67684ec351fca61bd69f7716/2/s8oo7ktm2gf1wvawqg3n.png")
                    .setThumbnailUrl("https://res.cloudinary.com/dstlbw3xa/image/upload/c_thumb,w_200,g_face/v1741956525/67684ec351fca61bd69f7716/2/s8oo7ktm2gf1wvawqg3n.png"),
            new Pin()
                    .setId("pin02")
                    .setAuthorId("user02")
                    .setType(Pin.PinType.IMAGE)
                    .setMediaUrl("https://res.cloudinary.com/dstlbw3xa/image/upload/v1741956521/67684ec351fca61bd69f7716/2/ufmcj7or3tp6wheumsfs.jpg")
                    .setThumbnailUrl("https://res.cloudinary.com/dstlbw3xa/image/upload/c_thumb,w_200,g_face/v1741956521/67684ec351fca61bd69f7716/2/ufmcj7or3tp6wheumsfs.jpg"),
            new Pin()
                    .setId("pin03")
                    .setAuthorId("user03")
                    .setType(Pin.PinType.IMAGE)
                    .setMediaUrl("https://res.cloudinary.com/dstlbw3xa/image/upload/v1735375550/675e9bcb4231a81f56b82c11/6/toz3hcn86mqcsins2gsx.png")
                    .setThumbnailUrl("https://res.cloudinary.com/dstlbw3xa/image/upload/c_thumb,w_200,g_face/v1735375550/675e9bcb4231a81f56b82c11/6/toz3hcn86mqcsins2gsx.png"),
            new Pin()
                    .setId("pin04")
                    .setAuthorId("user04")
                    .setType(Pin.PinType.IMAGE)
                    .setMediaUrl("https://res.cloudinary.com/dstlbw3xa/image/upload/v1735304610/676ea599cd4b26dc7654ba09/cover/d6euwabyejfnqyslk3cl.png")
                    .setThumbnailUrl("https://res.cloudinary.com/dstlbw3xa/image/upload/c_thumb,w_200,g_face/v1735304610/676ea599cd4b26dc7654ba09/cover/d6euwabyejfnqyslk3cl.png"),
            new Pin()
                    .setId("pin05")
                    .setAuthorId("user05")
                    .setType(Pin.PinType.IMAGE)
                    .setMediaUrl("https://res.cloudinary.com/dstlbw3xa/image/upload/v1735305519/676ea8facd4b26dc7654c093/1/b0pqu9vtrriylx80qqol.png")
                    .setThumbnailUrl("https://res.cloudinary.com/dstlbw3xa/image/upload/c_thumb,w_200,g_face/v1735305519/676ea8facd4b26dc7654c093/1/b0pqu9vtrriylx80qqol.png"),
            new Pin()
                    .setId("pin06")
                    .setAuthorId("user06")
                    .setType(Pin.PinType.IMAGE)
                    .setMediaUrl("https://res.cloudinary.com/dstlbw3xa/image/upload/v1735306508/676eab58cd4b26dc7654c1ca/1/fg1obhmbeixukqrpsvpf.png")
                    .setThumbnailUrl("https://res.cloudinary.com/dstlbw3xa/image/upload/c_thumb,w_200,g_face/v1735306508/676eab58cd4b26dc7654c1ca/1/fg1obhmbeixukqrpsvpf.png"),
            new Pin()
                    .setId("pin07")
                    .setAuthorId("user07")
                    .setType(Pin.PinType.IMAGE)
                    .setMediaUrl("https://res.cloudinary.com/dstlbw3xa/image/upload/v1735306081/676eab58cd4b26dc7654c1ca/cover/myrcpzkfatskgrc49y6f.png")
                    .setThumbnailUrl("https://res.cloudinary.com/dstlbw3xa/image/upload/c_thumb,w_200,g_face/v1735306081/676eab58cd4b26dc7654c1ca/cover/myrcpzkfatskgrc49y6f.png"),
            new Pin()
                    .setId("pin08")
                    .setAuthorId("user08")
                    .setType(Pin.PinType.IMAGE)
                    .setMediaUrl("https://res.cloudinary.com/dstlbw3xa/image/upload/v1735305097/676ea65bcd4b26dc7654bc0a/1/ascdenxcybbktydym0qf.png")
                    .setThumbnailUrl("https://res.cloudinary.com/dstlbw3xa/image/upload/c_thumb,w_200,g_face/v1735305097/676ea65bcd4b26dc7654bc0a/1/ascdenxcybbktydym0qf.png"),
            new Pin()
                    .setId("pin09")
                    .setAuthorId("user09")
                    .setType(Pin.PinType.IMAGE)
                    .setMediaUrl("https://res.cloudinary.com/dstlbw3xa/image/upload/v1735121033/67684e7751fca61bd69f762f/cover/ttp3u0rkx4nqzugwjjqw.png")
                    .setThumbnailUrl("https://res.cloudinary.com/dstlbw3xa/image/upload/c_thumb,w_200,g_face/v1735121033/67684e7751fca61bd69f762f/cover/ttp3u0rkx4nqzugwjjqw.png"),
            new Pin()
                    .setId("pin10")
                    .setAuthorId("user10")
                    .setType(Pin.PinType.IMAGE)
                    .setMediaUrl("https://res.cloudinary.com/dstlbw3xa/image/upload/v1734364422/avatar/6706517c0b92f958b833e64c/xz66lv3pdvaaektnouuy.png")
                    .setThumbnailUrl("https://res.cloudinary.com/dstlbw3xa/image/upload/c_thumb,w_200,g_face/v1734364422/avatar/6706517c0b92f958b833e64c/xz66lv3pdvaaektnouuy.png"),
            new Pin()
                    .setId("pin11")
                    .setAuthorId("user11")
                    .setType(Pin.PinType.IMAGE)
                    .setMediaUrl("https://res.cloudinary.com/dstlbw3xa/image/upload/v1735305474/676ea8facd4b26dc7654c093/cover/w2zfkqxrfyvewyh6jzhn.png")
                    .setThumbnailUrl("https://res.cloudinary.com/dstlbw3xa/image/upload/c_thumb,w_200,g_face/v1735305474/676ea8facd4b26dc7654c093/cover/w2zfkqxrfyvewyh6jzhn.png"),
            new Pin()
                    .setId("pin12")
                    .setAuthorId("user12")
                    .setType(Pin.PinType.IMAGE)
                    .setMediaUrl("https://res.cloudinary.com/dstlbw3xa/image/upload/v1735304864/676ea599cd4b26dc7654ba09/1/bj9lp4afe5v6ufb4lrcf.png")
                    .setThumbnailUrl("https://res.cloudinary.com/dstlbw3xa/image/upload/c_thumb,w_200,g_face/v1735304864/676ea599cd4b26dc7654ba09/1/bj9lp4afe5v6ufb4lrcf.png")
    ));

    public Pin(Parcel in) {
        super();
        readFromParcel(in);
    }

    public static final Parcelable.Creator<Pin> CREATOR = new Parcelable.Creator<Pin>() {

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
        this.id = in.readString();
        this.mediaUrl = in.readString();
        this.authorId = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.mediaUrl);
        dest.writeString(this.authorId);
    }
}
