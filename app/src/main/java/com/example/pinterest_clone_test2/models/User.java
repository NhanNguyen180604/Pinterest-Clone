package com.example.pinterest_clone_test2.models;

import com.example.pinterest_clone_test2.models.Pin;

import java.io.Serializable;

public class User implements Serializable {
    public enum Role{
        ADMIN,
        USER,
    }
    public enum Gender{
        Nam,
        Nữ,

        Khác,
    }
    private String id;
    private String username;
    private String password;
    private String email;
    private String firstName;
    private String lastName;

    private String birthDate;

    private Gender gender;

    private Role role;


    public User() {
    }

    public User(String username, String password, String email, String firstName, String lastName, String birthDate, Gender gender, Role role) {
        this.password = password;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthDate = birthDate;
        this.gender = gender;
        this.role = Role.USER;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    public String getPassword() {
        return password;
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

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
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

    public static boolean isValidEnum(String value) {
        try {
            Gender.valueOf(value);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
