package com.example.pinterest_clone_test2.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.example.pinterest_clone_test2.R;

import java.util.Arrays;
import java.util.List;

public class Pin implements Parcelable {
    private String id;
    private int media_url;  // for testing
    private String author_id;

    public Pin(String id, int media_url, String author_id) {
        this.id = id;
        this.media_url = media_url;
        this.author_id = author_id;
    }

    public String getId() {
        return id;
    }

    public String getAuthorId() {
        return author_id;
    }

    public int getMediaURL() {
        return media_url;
    }

    public static List<Pin> testData = Arrays.asList(
            new Pin("image_01", R.drawable.araragi, "author_01"),
            new Pin("image_02", R.drawable.cow, "author_02"),
            new Pin("image_03", R.drawable.conversation, "author_03"),
            new Pin("image_04", R.drawable.kaeya, "author_04"),
            new Pin("image_05", R.drawable.araragi, "author_05"),
            new Pin("image_06", R.drawable.cow, "author_06"),
            new Pin("image_07", R.drawable.conversation, "author_07"),
            new Pin("image_08", R.drawable.kaeya, "author_08"),
            new Pin("image_09", R.drawable.araragi, "author_09"),
            new Pin("image_10", R.drawable.cow, "author_10"),
            new Pin("image_11", R.drawable.conversation, "author_11"),
            new Pin("image_12", R.drawable.kaeya, "author_12")
    );

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
        this.media_url = in.readInt();
        this.author_id = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeInt(this.media_url);
        dest.writeString(this.author_id);
    }
}
