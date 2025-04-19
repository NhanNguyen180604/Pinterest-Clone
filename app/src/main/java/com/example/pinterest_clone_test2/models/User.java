package com.example.pinterest_clone_test2.models;


import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import com.example.pinterest_clone_test2.BR;

import java.util.ArrayList;
import java.util.List;

public class User extends BaseObservable implements Parcelable {
    @SuppressWarnings("unused")
    public enum Role {
        ADMIN,
        USER,
    }

    public enum Gender {
        Nam,
        Nữ,
        Khác,
    }

    private String id;
    private String email;
    private String firstName;
    private String lastName;
    private String birthDate;
    private Gender gender;
    private Role role;
    private String avatarUrl;
    private List<String> interests;

    public User() {
        interests = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Bindable
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
        notifyPropertyChanged(BR.firstName);
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = Gender.valueOf(gender);
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    @Bindable
    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
        notifyPropertyChanged(BR.avatarUrl);
    }

    @Bindable
    public List<String> getInterests() {
        return interests;
    }

    public void setInterests(List<String> interests) {
        this.interests = interests != null ? interests : new ArrayList<>();
        notifyPropertyChanged(BR.interests);
    }

    public static boolean isValidEnum(String value) {
        try {
            Gender.valueOf(value);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public User(Parcel in) {
        id = in.readString();
        email = in.readString();
        firstName = in.readString();
        lastName = in.readString();
        birthDate = in.readString();
        gender = (Gender) in.readSerializable();
        role = (Role) in.readSerializable();
        avatarUrl = in.readString();
        interests = new ArrayList<>();
        in.readStringList(interests);
    }

    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<>() {
        @Override
        public User createFromParcel(Parcel source) {
            return new User(source);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(email);
        parcel.writeString(firstName);
        parcel.writeString(lastName);
        parcel.writeString(birthDate);
        parcel.writeSerializable(gender);
        parcel.writeSerializable(role);
        parcel.writeString(avatarUrl);
        parcel.writeStringList(interests);
    }
}