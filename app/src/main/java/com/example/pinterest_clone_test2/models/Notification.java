package com.example.pinterest_clone_test2.models;

public class Notification {
    String title;
    int imageSource;
    int timestamp;

    public Notification(String title, int imageSource, int timestamp) {
        this.title = title;
        this.imageSource = imageSource;
        this.timestamp = timestamp;
    }

    public String getTitle() {
        return title;
    }

    public int getImageSource() {
        return imageSource;
    }

    public int getTimestamp() {
        return timestamp;
    }
}
