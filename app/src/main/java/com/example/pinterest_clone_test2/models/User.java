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
    public enum Role {
        Admin,
        User,
    }

    public enum Gender {
        Nam,
        Nữ,
        Khác,
    }

    private String userId;
    private String email;
    private String name;
    private String birthDate;
    private Gender gender;
    private Role role;
    private String avatarUrl;
    private List<String> interests;

    public User() {
        interests = new ArrayList<>();
    }

    // Getter, Setter
    @Bindable
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
        notifyPropertyChanged(BR.userId);
    }

    @Bindable
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
        notifyPropertyChanged(BR.email);
    }

    @Bindable
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        notifyPropertyChanged(BR.name);
    }

    @Bindable
    public Gender getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = Gender.valueOf(gender);
        notifyPropertyChanged(BR.gender);
    }

    @Bindable
    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    @Bindable
    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
        notifyPropertyChanged(BR.role);
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

    public boolean isBanned() {
        return false; // Thêm logic kiểm tra trạng thái ban ở đây
    }

    // Implement Interface
    public User(Parcel in) {
        userId = in.readString();
        email = in.readString();
        name = in.readString();
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
        parcel.writeString(userId);
        parcel.writeString(email);
        parcel.writeString(name);
        parcel.writeString(birthDate);
        parcel.writeSerializable(gender);
        parcel.writeSerializable(role);
        parcel.writeString(avatarUrl);
        parcel.writeStringList(interests);
    }
}