package com.example;

public enum Season {
    SPRING("warm"),
    SUMMER("hot"),
    AUTUMN("cool"),
    WINTER("cold");

    private final String description;

    Season(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
