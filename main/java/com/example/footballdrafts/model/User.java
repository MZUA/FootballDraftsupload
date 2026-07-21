package com.example.footballdrafts.model;

public class User {
    private String uid;
    private String name;
    private String email;
    private String preferences; // e.g., preferred position, skill level

    public User() {} // Needed for Firestore

    public User(String uid, String name, String email, String preferences) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.preferences = preferences;
    }

    // Getters and Setters
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPreferences() { return preferences; }
    public void setPreferences(String preferences) { this.preferences = preferences; }
}
