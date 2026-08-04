package com.example.al_mirath.minigame;

import javafx.scene.Node;

import java.util.function.Consumer;

/**
 * A short skill challenge the player attempts to rewind a decision.
 *
 * <p>Implementations build their own view and report exactly one result
 * through the callback. The host overlay owns presentation around the game
 * (title, instructions, result banner) so each game only worries about play.
 */
public interface MiniGame {

    /** Display name, e.g. "The Scribe's Hand". */
    String getTitle();

    /** One or two sentences of in-world framing. */
    String getFlavourText();

    /** Plain rules text shown before the player begins. */
    String getInstructions();

    /** Builds the playable area. Called once per attempt. */
    Node buildView();

    /** Begins play. Called after the view is attached to the scene. */
    void start();

    /** Stops timers and animations. Safe to call more than once. */
    void stop();

    /** Registers the callback that receives the single result. */
    void setOnFinished(Consumer<MiniGameResult> onFinished);
}
