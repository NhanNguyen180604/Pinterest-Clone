package com.example.pinterest_clone_test2.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.Exclude;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Board implements Parcelable {
    String id;
    String name;
    String description;
    String authorId;
    boolean isPublic;
    List<String> collaborators;
    List<String> pins;
    @Exclude
    List<Pin> pinsObj;

    public Board() {
    }

    public String getId() {
        return id;
    }

    public Board setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public Board setName(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public Board setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getAuthorId() {
        return authorId;
    }

    public Board setAuthorId(String authorId) {
        this.authorId = authorId;
        return this;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public Board setPublic(boolean aPublic) {
        isPublic = aPublic;
        return this;
    }

    public List<String> getPins() {
        return pins;
    }

    public Board setPins(List<String> pins) {
        this.pins = pins;
        return this;
    }

    public List<Pin> getPinsObj() {
        return pinsObj;
    }

    public Board setPinsObj(List<Pin> pinsObj) {
        this.pinsObj = pinsObj;
        return this;
    }

    public List<String> getCollaborators() {
        return collaborators;
    }

    public Board setCollaborators(List<String> collaborators) {
        this.collaborators = collaborators;
        return this;
    }

    public static List<Board> ideaBoardSeedData = new ArrayList<>(Arrays.asList(
            new Board()
                    .setId("seed-id")
                    .setAuthorId(null)
                    .setName("Cute art")
                    .setDescription(null)
                    .setPublic(true)
                    .setPins(new ArrayList<>()),
            new Board()
                    .setId("seed-id")
                    .setAuthorId(null)
                    .setName("Anime")
                    .setDescription(null)
                    .setPublic(true)
                    .setPins(new ArrayList<>()),
            new Board()
                    .setId("seed-id")
                    .setAuthorId(null)
                    .setName("Chilling music")
                    .setDescription(null)
                    .setPublic(true)
                    .setPins(new ArrayList<>()),
            new Board()
                    .setId("seed-id")
                    .setAuthorId(null)
                    .setName("Madness")
                    .setDescription(null)
                    .setPublic(true)
                    .setPins(new ArrayList<>()),
            new Board()
                    .setId("seed-id")
                    .setAuthorId(null)
                    .setName("Anatomy")
                    .setDescription(null)
                    .setPublic(true)
                    .setPins(new ArrayList<>())
    ));

    public static final Creator<Board> CREATOR = new Creator<Board>() {
        @Override
        public Board createFromParcel(Parcel in) {
            return new Board(in);
        }

        @Override
        public Board[] newArray(int size) {
            return new Board[size];
        }
    };

    private Board(Parcel in) {
        id = in.readString();
        name = in.readString();
        description = in.readString();
        authorId = in.readString();
        isPublic = in.readBoolean();
        pinsObj = in.createTypedArrayList(Pin.CREATOR);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(name);
        parcel.writeString(description);
        parcel.writeString(authorId);
        parcel.writeBoolean(isPublic);
        parcel.writeParcelableList(pinsObj, i);
    }
}
