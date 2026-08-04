package com.example.al_mirath.minigame;

/**
 * Outcome of a single Threads of Fate challenge.
 *
 * @param success  whether the player earned the rewind
 * @param score    points scored, shown on the result screen
 * @param summary  one line describing how the attempt went
 */
public record MiniGameResult(boolean success, int score, String summary) {

    public static MiniGameResult won(int score, String summary) {
        return new MiniGameResult(true, score, summary);
    }

    public static MiniGameResult lost(int score, String summary) {
        return new MiniGameResult(false, score, summary);
    }
}
