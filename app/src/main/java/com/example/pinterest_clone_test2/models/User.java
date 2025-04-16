package com.example.pinterest_clone_test2.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import com.example.pinterest_clone_test2.BR;
import com.google.firebase.firestore.DocumentId;

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
    public enum Mode {
        NORMAL,
        BANNED,
    }
    private String userId;
    private String password;
    private String email;
    private String name;
    private String birthDate;
    private Gender gender;
    private Role role;
    private String avatarUrl;

    // Constructor
    public User() {}
    public User(String password, String email, String name, String birthDate, Gender gender, Role role) {
        this.password = password;
        this.email = email;
        this.name = name;
        this.birthDate = birthDate;
        this.gender = gender;
        this.role = role;
    }
    public User(String password, String email, String name, String birthDate, Gender gender) {
        this.password = password;
        this.email = email;
        this.name = name;
        this.birthDate = birthDate;
        this.gender = gender;
        this.role = Role.User;
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
    public void setPassword(String password) {
        this.password = password;
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

    public boolean isBanned() {
        return false; // Thêm logic kiểm tra trạng thái ban ở đây
    }

    // Implement Interface
    public User(Parcel in) {
        email = in.readString();
        name = in.readString();
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
        parcel.writeString(name);
        parcel.writeString(birthDate);
        parcel.writeSerializable(gender);
        parcel.writeSerializable(role);
        parcel.writeString(avatarUrl);
    }
}
