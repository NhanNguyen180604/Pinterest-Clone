package com.example.pinterest_clone_test2.models;


import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import com.example.pinterest_clone_test2.BR;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private String password;
    private String email;
    private String firstName;
    private String lastName;
    private String birthDate;
    private Gender gender;
    private Role role;
    private String avatarUrl;

    // Danh sách mock users
    private static List<User> mockUsers = new ArrayList<>(Arrays.asList(
            new User("password1", "user1@example.com", "Tân", "01/01/1990", Gender.Nam),
            new User("password2", "user2@example.com", "Tandy", "02/02/1992", Gender.Nữ),
            new User("password3", "user3@example.com", "Võ", "03/03/1995", Gender.Khác)
    ));
    private static Map<String, String> tokenMap = new HashMap<>();

    public User() {
    }

    public User(String password, String email, String firstName, String lastName, String birthDate, Gender gender, Role role) {
        this.password = password;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthDate = birthDate;
        this.gender = gender;
        this.role = role;
    }

    public User(String password, String email, String firstName, String birthDate, Gender gender, Role role) {
        this.password = password;
        this.email = email;
        this.firstName = firstName;
        this.birthDate = birthDate;
        this.gender = gender;
        this.role = role;
    }

    public User(String password, String email, String firstName, String birthDate, Gender gender) {
        this.password = password;
        this.email = email;
        this.firstName = firstName;
        this.birthDate = birthDate;
        this.gender = gender;
        this.role = Role.USER;
    }


    public void setPassword(String password) {
        this.password = password;
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

    public static boolean isValidEnum(String value) {
        try {
            Gender.valueOf(value);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public User(Parcel in) {
        email = in.readString();
        firstName = in.readString();
        lastName = in.readString();
        birthDate = in.readString();
        gender = (Gender) in.readSerializable();
        role = (Role) in.readSerializable();
        avatarUrl = in.readString();
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
        parcel.writeString(email);
        parcel.writeString(firstName);
        parcel.writeString(lastName);
        parcel.writeString(birthDate);
        parcel.writeSerializable(gender);
        parcel.writeSerializable(role);
        parcel.writeString(avatarUrl);
    }
}
