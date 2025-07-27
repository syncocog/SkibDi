package com.voidlight.event.models;

/**
 * Enum representing the two teams in a match
 */
public enum Team {
    RED("Red", "#FF5555"),
    BLUE("Blue", "#5555FF");
    
    private final String displayName;
    private final String color;
    
    Team(String displayName, String color) {
        this.displayName = displayName;
        this.color = color;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getColor() {
        return color;
    }
    
    public String getColoredName() {
        return "<" + color + ">" + displayName;
    }
}