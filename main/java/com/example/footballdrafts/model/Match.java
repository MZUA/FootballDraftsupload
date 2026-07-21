package com.example.footballdrafts.model;

import com.google.firebase.Timestamp; // Use Firestore's Timestamp
import com.google.firebase.firestore.GeoPoint;

public class Match {
    private String matchId;        // Auto-generated Firestore ID
    private String hostUserId;     // UID of the user who created the match
    private String hostLineupId;   // ID of the host's lineup (will be their UID)
    private String hostName;       // Name of the host user (for display)

    private Timestamp dateTime;      // Date and time of the match
    private String locationName;   // e.g., "Goals Wembley"
    private String locationAddress; // Full address
    private GeoPoint locationGeoPoint; // For map display and geo-queries

    private String status;         // e.g., "pending_opponent", "confirmed", "completed", "cancelled"

    // Fields for the opponent when a request is accepted
    private String opponentUserId;
    private String opponentLineupId;
    private String opponentName;

    public Match() {
        // Firestore requires a public no-arg constructor
    }

    // Constructor for initial creation by host
    public Match(String matchId, String hostUserId, String hostLineupId, String hostName, Timestamp dateTime,
                 String locationName, String locationAddress, GeoPoint locationGeoPoint) {
        this.matchId = matchId;
        this.hostUserId = hostUserId;
        this.hostLineupId = hostLineupId;
        this.hostName = hostName;
        this.dateTime = dateTime;
        this.locationName = locationName;
        this.locationAddress = locationAddress;
        this.locationGeoPoint = locationGeoPoint;
        this.status = "pending_opponent"; // Default status when created
    }

    // Getters and Setters for all fields (important for Firestore)
    public String getMatchId() { return matchId; }
    public void setMatchId(String matchId) { this.matchId = matchId; }
    public String getHostUserId() { return hostUserId; }
    public void setHostUserId(String hostUserId) { this.hostUserId = hostUserId; }
    public String getHostLineupId() { return hostLineupId; }
    public void setHostLineupId(String hostLineupId) { this.hostLineupId = hostLineupId; }
    public String getHostName() { return hostName; }
    public void setHostName(String hostName) { this.hostName = hostName; }
    public Timestamp getDateTime() { return dateTime; }
    public void setDateTime(Timestamp dateTime) { this.dateTime = dateTime; }
    public String getLocationName() { return locationName; }
    public void setLocationName(String locationName) { this.locationName = locationName; }
    public String getLocationAddress() { return locationAddress; }
    public void setLocationAddress(String locationAddress) { this.locationAddress = locationAddress; }
    public GeoPoint getLocationGeoPoint() { return locationGeoPoint; }
    public void setLocationGeoPoint(GeoPoint locationGeoPoint) { this.locationGeoPoint = locationGeoPoint; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getOpponentUserId() { return opponentUserId; }
    public void setOpponentUserId(String opponentUserId) { this.opponentUserId = opponentUserId; }
    public String getOpponentLineupId() { return opponentLineupId; }
    public void setOpponentLineupId(String opponentLineupId) { this.opponentLineupId = opponentLineupId; }
    public String getOpponentName() { return opponentName; }
    public void setOpponentName(String opponentName) { this.opponentName = opponentName; }
}