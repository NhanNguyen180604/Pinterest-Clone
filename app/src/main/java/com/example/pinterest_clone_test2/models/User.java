package com.example.pinterest_clone_test2.models;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class User {
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
