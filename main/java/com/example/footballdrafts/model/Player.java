package com.example.footballdrafts.model;

public class Player {
    private String firstName;
    private String lastName;

    // Position will be the key in the map, so not strictly needed here
    // but can be useful if you ever pull a player out of context.

    private String assignedPosition;
    public Player() {} // For Firestore
    public Player(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    // Getters and Setters
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getAssignedPosition() { return assignedPosition; }
    public void setAssignedPosition(String assignedPosition) { this.assignedPosition = assignedPosition; }

    public String getFullName() {
        return (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
    }

    public String getInitials() {
        String firstInitial = (firstName != null && !firstName.isEmpty()) ? firstName.substring(0, 1) : "";
        String lastInitial = (lastName != null && !lastName.isEmpty()) ? lastName.substring(0, 1) : "";
        return firstInitial + lastInitial;
    }
}
