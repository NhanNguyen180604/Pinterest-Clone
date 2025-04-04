package com.example.pinterest_clone_test2.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Board implements Parcelable {
    String id;
    String name;
    String description;
    String authorId;
    boolean isPublic;
    //    List<User> collaborators;
    List<Pin> pins;
//    List<Collage> collages;

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

    public List<Pin> getPins() {
        return pins;
    }

    public Board setPins(List<Pin> pins) {
        this.pins = pins;
        return this;
    }

    public static List<Board> seedData = new ArrayList<>(Arrays.asList(
            new Board()
                    .setId("board01")
                    .setAuthorId("user01")
                    .setName("Tuyu")
                    .setDescription("Tuyu")
                    .setPublic(true)
                    .setPins(Pin.testData.subList(0, 4)),
            new Board()
                    .setId("board02")
                    .setAuthorId("user02")
                    .setName("Troll")
                    .setDescription("tron tron")
                    .setPublic(true)
                    .setPins(Pin.testData.subList(2, 5)),
            new Board()
                    .setId("board03")
                    .setAuthorId("user03")
                    .setName("Who is Amanai")
                    .setDescription("Why is Gojo apologizing")
                    .setPublic(true)
                    .setPins(Pin.testData.subList(3, 7)),
            new Board()
                    .setId("board04")
                    .setAuthorId("user04")
                    .setName("Lucilius")
                    .setDescription("Father of all Primal Beasts")
                    .setPublic(true)
                    .setPins(Pin.testData.subList(0, 11))
    ));

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
        pins = in.createTypedArrayList(Pin.CREATOR);
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
        parcel.writeParcelableList(pins, i);
    }
}
