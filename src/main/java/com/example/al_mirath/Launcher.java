package com.example.al_mirath;

/**
 * The entry point a packaged build starts from.
 *
 * <p>{@link Main} extends {@code Application}, and the JVM refuses to launch a
 * class that does when JavaFX is on the classpath rather than the module path —
 * it checks for the JavaFX runtime as a set of modules and stops with "JavaFX
 * runtime components are missing" before any of our code runs. A self-contained
 * jar has everything in one place on the classpath by definition, so it needs a
 * main class that is not an {@code Application} to get past that check and
 * start the toolkit the ordinary way.
 *
 * <p>Running from source with {@code javafx:run} still goes through
 * {@link Main} on the module path and never touches this.
 */
public final class Launcher {

    private Launcher() {
    }

    public static void main(String[] args) {
        Main.main(args);
    }
}
