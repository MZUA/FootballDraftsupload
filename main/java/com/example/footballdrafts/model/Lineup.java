package com.example.footballdrafts.model;

import java.util.Map;
import java.util.HashMap;

public class Lineup {
    private String lineupId; // This will be the userId
    private String userId;   // This will also be the userId, for consistency in queries if ever needed
    private String lineupName;
    private Map<String, Player> playerPositions; // Key: Position (e.g., "ST"), Value: Player object

    public Lineup() {
        this.playerPositions = new HashMap<>();
    }

    // Constructor when creating/fetching for a specific user
    public Lineup(String userId, String lineupName) {
        this.lineupId = userId; // lineupId is the userId
        this.userId = userId;
        this.lineupName = lineupName;
        this.playerPositions = new HashMap<>();
    }


    // Getters and Setters
    public String getLineupId() { return lineupId; }
    public void setLineupId(String lineupId) { this.lineupId = lineupId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getLineupName() { return lineupName; }
    public void setLineupName(String lineupName) { this.lineupName = lineupName; }

    public Map<String, Player> getPlayerPositions() { return playerPositions; }
    public void setPlayerPositions(Map<String, Player> playerPositions) { this.playerPositions = playerPositions; }
}