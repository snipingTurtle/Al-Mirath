package com.example.al_mirath.model;

public class EndingResult {

    private final String title;
    private final String description;

    public EndingResult(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }
}