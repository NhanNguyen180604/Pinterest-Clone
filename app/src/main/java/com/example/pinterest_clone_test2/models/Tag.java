package com.example.pinterest_clone_test2.models;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Tag {
    private String id;
    private String name;
    private int count;
    private long createdAt;
    private List<String> pinIds; // List of pin IDs associated with this tag

    // Default constructor for Firestore
    public Tag() {
        pinIds = new ArrayList<>();
    }

    // Constructor with parameters
    public Tag(String id, String name, int count, long createdAt) {
        this.id = id;
        this.name = name;
        this.count = count;
        this.createdAt = createdAt;
        this.pinIds = new ArrayList<>();
    }

    // Constructor with pinIds
    public Tag(String id, String name, int count, long createdAt, List<String> pinIds) {
        this.id = id;
        this.name = name;
        this.count = count;
        this.createdAt = createdAt;
        this.pinIds = pinIds != null ? pinIds : new ArrayList<>();
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public List<String> getPinIds() {
        return pinIds;
    }

    public void setPinIds(List<String> pinIds) {
        this.pinIds = pinIds != null ? pinIds : new ArrayList<>();
    }

    public boolean addPinId(String pinId) {
        if (pinIds == null) {
            pinIds = new ArrayList<>();
        }

        if (!pinIds.contains(pinId)) {
            pinIds.add(pinId);
            count++;
            return true;
        }
        return false;
    }

    public boolean removePinId(String pinId) {
        if (pinIds != null && pinIds.contains(pinId)) {
            pinIds.remove(pinId);
            count = Math.max(0, count - 1);
            return true;
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tag tag = (Tag) o;
        return Objects.equals(name, tag.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @NonNull
    @Override
    public String toString() {
        return name;
    }
}