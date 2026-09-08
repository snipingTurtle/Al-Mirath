package com.example.al_mirath.model;

import java.util.Objects;

public record NpcMemory(
        String id,
        String description,
        int playerAge,
        int emotionalWeight
) {

    /**
     * The age recorded for something that happened before the player existed.
     *
     * <p>An heir inherits the standing their house has with people, not the
     * afternoons that built it. Those memories are real and still shape how
     * they are treated — they just cannot be told as "you were nine", because
     * the person being told was not born.
     */
    public static final int BEFORE_YOUR_TIME = -1;

    /** True for something the house did, rather than the player. */
    public boolean predatesThePlayer() {
        return playerAge < 0;
    }

    public NpcMemory {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(description, "description");

        emotionalWeight = Math.max(
                -100,
                Math.min(100, emotionalWeight)
        );
    }
}
