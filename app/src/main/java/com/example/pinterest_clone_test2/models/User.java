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
    public static List<User> mockUsers = new ArrayList<>(Arrays.asList(
            new User("password1", "user1@example.com", "Tân", "01/01/1990", Gender.Nam),
            new User("password2", "user2@example.com", "Tandy", "02/02/1992", Gender.Nữ),
            new User("password3", "user3@example.com", "Võ", "03/03/1995", Gender.Khác)
    ));
    public static Map<String, String> tokenMap = new HashMap<>();

    // Constructor
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


    // Getter, Setter
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

    // Phương thức kiểm tra
    public static boolean isValidEnum(String value) {
        try {
            Gender.valueOf(value);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    // Phương thức kiểm tra email có tồn tại hay không
    public static boolean isEmailExists(String email) {
        for (User user : mockUsers) {
            if (user.getEmail().equalsIgnoreCase(email)) {
                return true;
            }
        }
        return false;
    }

    public static String login(String email, String password) {
        for (User user : mockUsers) {
            if (user.getEmail().equalsIgnoreCase(email) && user.getPassword().equals(password)) {
                String token = UUID.randomUUID().toString();
                tokenMap.put(token, email); // Lưu token và email vào Map
                return token;
            }
        }
        return null;
    }

    public static void initializeToken(String token, String email) {
        tokenMap.put(token, email);
    }
    public static String register(User user) {
        mockUsers.add(user);
        String token = UUID.randomUUID().toString();
        tokenMap.put(token, user.getEmail());
        return token;
    }

    // Lấy thông tin người dùng từ token
    public static UserInfo getUserByToken(String token) {
        String email = tokenMap.get(token); // Lấy email từ token
        if (email != null) {
            for (User user : mockUsers) {
                if (user.getEmail().equalsIgnoreCase(email)) {
                    // Trả về UserInfo thay vì User, không chứa password
                    return new UserInfo(user.getEmail(), user.getFirstName(), user.getLastName(),
                            user.getBirthDate(), user.getGender(), user.getRole());
                }
            }
        }
        return null;
    }

    public static class UserInfo implements Serializable {
        private final String email;
        private final String firstName;
        private final String lastName;
        private final String birthDate;
        private final Gender gender;
        private final Role role;

        public UserInfo(String email, String firstName, String lastName, String birthDate, Gender gender, Role role) {
            this.email = email;
            this.firstName = firstName;
            this.lastName = lastName;
            this.birthDate = birthDate;
            this.gender = gender;
            this.role = role;
        }

        // Getter cho các thuộc tính
        public String getEmail() { return email; }
        public String getFirstName() { return firstName; }
        public String getLastName() { return lastName; }
        public String getBirthDate() { return birthDate; }
        public Gender getGender() { return gender; }
        public Role getRole() { return role; }
    }

}
