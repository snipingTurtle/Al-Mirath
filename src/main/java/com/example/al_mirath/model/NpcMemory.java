package com.example.al_mirath.model;

import java.util.Objects;

public record NpcMemory(
        String id,
        String description,
        int playerAge,
        int emotionalWeight
) {

    public NpcMemory {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(description, "description");

        emotionalWeight = Math.max(
                -100,
                Math.min(100, emotionalWeight)
        );
    }
}
