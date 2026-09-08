package com.example.al_mirath.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Finding the artwork when the game is a file rather than a folder.
 *
 * <p>The backgrounds are discovered by walking the resource directory, which
 * worked in every development build and in every test, and produced a game
 * with no artwork at all the first time it was packaged: a resource inside a
 * jar has no file path, and asking for one throws. Nothing caught it because
 * nothing had ever run the packaged artifact.
 *
 * <p>This builds a jar and reads it, which is the only way to test the branch
 * that only exists when the game ships.
 */
class PackagedResourcesTest {

    private static final String BASE = "/com/example/al_mirath/images_jpg/";

    /** A jar laid out the way a packaged build is. */
    private Path aJarContaining(Path where, String... entries) throws Exception {
        Path jar = where.resolve("almirath.jar");

        try (OutputStream out = Files.newOutputStream(jar);
             JarOutputStream jarOut = new JarOutputStream(out)) {

            for (String entry : entries) {
                jarOut.putNextEntry(new JarEntry(entry));

                if (!entry.endsWith("/")) {
                    jarOut.write(new byte[]{1, 2, 3});
                }

                jarOut.closeEntry();
            }
        }

        return jar;
    }

    private URL resourceUrlInside(Path jar, String resourcePath) throws Exception {
        return new URL("jar:" + jar.toUri() + "!" + resourcePath);
    }

    @Test
    @DisplayName("the artwork is found inside a packaged jar")
    void imagesAreFoundInAJar(@TempDir Path where) throws Exception {
        String base = BASE.substring(1);

        Path jar = aJarContaining(
                where,
                base,
                base + "00_menu/",
                base + "00_menu/main_menu_01.jpg",
                base + "00_menu/main_menu_02.png",
                base + "01_birth/birth_01.jpeg",
                base + "00_menu/notes.txt",
                "com/example/al_mirath/fxml/game-screen.fxml"
        );

        // Through the same dispatch the game uses, not straight to the jar
        // reader: choosing the wrong one of the two is the mistake that
        // shipped a game with no artwork.
        List<String> found = BackgroundLibrary.imagesUnder(
                resourceUrlInside(jar, BASE + "00_menu"), BASE + "00_menu");

        assertEquals(
                2, found.size(),
                "expected the two images in that folder and nothing else: " + found
        );

        assertTrue(
                found.contains(BASE + "00_menu/main_menu_01.jpg"),
                "an image came back under a path the game cannot load it from: " + found
        );

        assertTrue(found.contains(BASE + "00_menu/main_menu_02.png"));

        for (String path : found) {
            assertTrue(
                    path.startsWith("/"),
                    path + " has no leading slash, so getResource will not find it"
            );
        }
    }

    @Test
    @DisplayName("a folder's images do not leak into another folder's list")
    void foldersStaySeparate(@TempDir Path where) throws Exception {
        String base = BASE.substring(1);

        Path jar = aJarContaining(
                where,
                base + "00_menu/main_menu_01.jpg",
                base + "01_birth/birth_01.jpg",
                base + "00_menu_extra/other.jpg"
        );

        List<String> menu = BackgroundLibrary.imagesUnder(
                resourceUrlInside(jar, BASE + "00_menu"), BASE + "00_menu");

        assertEquals(1, menu.size(), "the menu folder picked up somebody else's art: " + menu);
        assertEquals(BASE + "00_menu/main_menu_01.jpg", menu.get(0));

        // And the whole set, from the root.
        List<String> everything = BackgroundLibrary.imagesUnder(
                resourceUrlInside(jar, BASE), BASE);

        assertEquals(3, everything.size(), "the root did not find every image: " + everything);
    }

    @Test
    @DisplayName("a jar path with a space in it is still readable")
    void aPathWithASpaceStillWorks(@TempDir Path where) throws Exception {
        Path awkward = where.resolve("My Games");
        Files.createDirectories(awkward);

        String base = BASE.substring(1);

        Path jar = aJarContaining(awkward, base + "00_menu/main_menu_01.jpg");

        List<String> found = BackgroundLibrary.imagesUnder(
                resourceUrlInside(jar, BASE + "00_menu"), BASE + "00_menu");

        assertFalse(
                found.isEmpty(),
                "a game installed anywhere with a space in the path found no artwork"
        );
    }

    /**
     * The everyday path, which is what every other test and every development
     * build exercises. Here so that a change made for the packaged case cannot
     * quietly break the one people work in.
     */
    @Test
    @DisplayName("the artwork is still found when running from a build directory")
    void imagesAreStillFoundOnDisk() {
        assertFalse(
                BackgroundLibrary.getMenuBackground().isBlank(),
                "the game found no menu artwork running from its own classes"
        );
    }
}
