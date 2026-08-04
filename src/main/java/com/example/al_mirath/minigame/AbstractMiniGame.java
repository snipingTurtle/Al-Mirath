package com.example.al_mirath.minigame;

import java.util.function.Consumer;

/**
 * Shared plumbing for mini-games: difficulty, the finish callback, and the
 * guarantee that a result is delivered exactly once.
 *
 * <p>Several of these games can finish from more than one place — a timer
 * expiring, a wrong answer, or the final correct answer all end a round. The
 * {@code finished} latch makes those paths safe to write independently without
 * risking a double rewind.
 */
public abstract class AbstractMiniGame implements MiniGame {

    /** 1 = gentle, 5 = brutal. Scales with how far the life has progressed. */
    protected final int difficulty;

    private Consumer<MiniGameResult> onFinished;
    private boolean finished = false;

    protected AbstractMiniGame(int difficulty) {
        this.difficulty = Math.max(1, Math.min(5, difficulty));
    }

    @Override
    public void setOnFinished(Consumer<MiniGameResult> onFinished) {
        this.onFinished = onFinished;
    }

    /**
     * Delivers the result once and ignores every later call.
     */
    protected void finish(MiniGameResult result) {
        if (finished) {
            return;
        }

        finished = true;
        stop();

        if (onFinished != null) {
            onFinished.accept(result);
        }
    }

    protected boolean isFinished() {
        return finished;
    }
}
