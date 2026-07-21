package com.example.footballdrafts.model;

import com.google.firebase.Timestamp;

public class MatchRequest {
    private String requestId; // Document ID of this request
    private String matchId; // ID of the match being requested
    private String requestingUserId;
    private String requestingUserName; // Name of the user making the request
    private String requestingUserLineupId; // Will be the requestingUser's UID
    private String hostUserId; // UID of the match host
    private String status; // "pending", "accepted", "declined"
    private Timestamp timestamp;

    public MatchRequest() {} // Firestore constructor

    public MatchRequest(String matchId, String requestingUserId, String requestingUserName, String requestingUserLineupId, String hostUserId) {
        this.matchId = matchId;
        this.requestingUserId = requestingUserId;
        this.requestingUserName = requestingUserName;
        this.requestingUserLineupId = requestingUserLineupId;
        this.hostUserId = hostUserId;
        this.status = "pending";
        this.timestamp = Timestamp.now();
    }

    // Getters and Setters for all fields
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    public String getMatchId() { return matchId; }
    public void setMatchId(String matchId) { this.matchId = matchId; }
    public String getRequestingUserId() { return requestingUserId; }
    public void setRequestingUserId(String requestingUserId) { this.requestingUserId = requestingUserId; }
    public String getRequestingUserName() { return requestingUserName; }
    public void setRequestingUserName(String requestingUserName) { this.requestingUserName = requestingUserName; }
    public String getRequestingUserLineupId() { return requestingUserLineupId; }
    public void setRequestingUserLineupId(String requestingUserLineupId) { this.requestingUserLineupId = requestingUserLineupId; }
    public String getHostUserId() { return hostUserId; }
    public void setHostUserId(String hostUserId) { this.hostUserId = hostUserId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Timestamp getTimestamp() { return timestamp; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }
}
