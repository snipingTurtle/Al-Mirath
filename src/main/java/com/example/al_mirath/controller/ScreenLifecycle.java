package com.example.al_mirath.controller;

/**
 * A screen controller that holds resources outliving its scene.
 *
 * <p>JavaFX animations run on a global timer, not per scene: an indefinite
 * {@code Timeline} started by a screen keeps firing after the stage has moved
 * on, holding its now-invisible scene graph — and its decoded backgrounds — in
 * memory. {@link com.example.al_mirath.Main} calls {@link #dispose()} on the
 * outgoing controller before installing the next screen so each screen's
 * animations end with it.
 */
public interface ScreenLifecycle {

    /** Stops anything this screen started. Must tolerate being called twice. */
    void dispose();
}
