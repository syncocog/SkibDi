package com.voidlight.event.models;

/**
 * Enum representing the different states of a match
 */
public enum MatchState {
    COUNTDOWN("Countdown"),
    IN_PROGRESS("In Progress"),
    ENDED("Ended");
    
    private final String displayName;
    
    MatchState(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}