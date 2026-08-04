package com.example.al_mirath.service;

import com.example.al_mirath.model.Choice;
import com.example.al_mirath.model.GameEvent;
import com.example.al_mirath.model.PlayerCharacter;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

public class BackgroundLibrary {

    private enum Category {
        MENU, BIRTH, COMMONER, FAMILY, PALACE, SCHOLAR, BAZAAR,
        MILITARY, SHADOW, EXILE, CRISIS, COURT, LEGACY, PRISON
    }

    private static final Random random = new Random();
    private static final String BASE = "/com/example/al_mirath/images_jpg/";
    private static String lastBackground = "";

    private static final List<String> ALL_IMAGES = loadCategory("");

    private static final List<String> MENU = loadCategory("00_menu");
    private static final List<String> BIRTH = loadCategory("01_birth");
    private static final List<String> COMMONER = loadCategory("02_commoner_village_street");
    private static final List<String> FAMILY = loadCategory("03_family_noble_house");
    private static final List<String> PALACE = loadCategory("04_palace_royal");
    private static final List<String> SCHOLAR = loadCategory("05_scholar_library");
    private static final List<String> BAZAAR = loadCategory("06_bazaar_merchant");
    private static final List<String> MILITARY = loadCategory("07_military_frontier");
    private static final List<String> SHADOW = loadCategory("08_shadow_spy_secret");
    private static final List<String> EXILE = loadCategory("09_exile_travel_gates");
    private static final List<String> CRISIS = loadCategory("10_crisis_unrest_famine");
    private static final List<String> COURT = loadCategory("11_court_trial_politics");
    private static final List<String> LEGACY = loadCategory("12_legacy_endings");
    private static final List<String> PRISON = loadCategory("13_prison");

    private static final List<String> FORCED_MENU = findPreferredImages(
            "menu_01",
            "palace_02",
            "huge_jamaat_01",
            "city_birds_eye_01",
            "landscape_01"
    );

    public static String getMenuBackground() {
        if (!FORCED_MENU.isEmpty()) return pick(FORCED_MENU);
        if (!MENU.isEmpty()) return pick(MENU);
        return pick(PALACE);
    }

    public static String getBirthBackground(PlayerCharacter player) {
        return pick(pool(getOriginCategory(player)));
    }

    public static String getBackgroundForEvent(GameEvent event, PlayerCharacter player) {
        return pick(pool(getEventCategory(event, player)));
    }

    public static String getConsequenceBackground(GameEvent event, Choice choice, PlayerCharacter player) {
        if (event == null || choice == null) {
            return pick(pool(Category.LEGACY));
        }

        Category eventCategory = getEventCategory(event, player);
        String choiceText = lower(choice.getText());

        if (containsAny(choiceText, "prison", "cell", "captured", "punished")) {
            return pick(pool(Category.PRISON));
        }

        if (containsAny(choiceText, "shadow network", "shadow help", "secret meeting", "blackmail")) {
            return pick(pool(Category.SHADOW));
        }

        if (containsAny(choiceText, "court", "throne", "crown", "ruler", "palace")) {
            return pick(pool(Category.PALACE));
        }

        if (containsAny(choiceText, "study", "scholar", "record", "debate", "lesson")) {
            return pick(pool(Category.SCHOLAR));
        }

        if (containsAny(choiceText, "merchant", "trade", "caravan", "buy", "sell")) {
            return pick(pool(Category.BAZAAR));
        }

        if (containsAny(choiceText, "soldier", "military", "commander", "barracks")) {
            return pick(pool(Category.MILITARY));
        }

        return pick(pool(eventCategory));
    }

    public static String getStatusBackground(PlayerCharacter player) {
        return pick(pool(getOriginCategory(player)));
    }

    private static Category getEventCategory(GameEvent event, PlayerCharacter player) {
        if (event == null) return Category.LEGACY;

        return switch (event.getTitle()) {
            case "The Tax Collector Arrives" -> Category.BAZAAR;
            case "The Court Tutor Arrives" -> Category.SCHOLAR;
            case "A Lesson in Survival" -> Category.COMMONER;
            case "The First Public Shame" -> Category.COMMONER;
            case "A Stranger Offers Help" -> Category.COMMONER;
            case "A Family Argument" -> Category.FAMILY;

            case "A Patron Notices You" -> Category.PALACE;
            case "The Barracks Test" -> Category.MILITARY;
            case "The Shape of Ambition" -> getOriginCategory(player);
            case "A Rival Appears" -> Category.FAMILY;
            case "First Taste of Authority" -> Category.COURT;
            case "A Dangerous Invitation" -> Category.SHADOW;

            case "The Merchant's Risk" -> Category.BAZAAR;
            case "The Palace Whisper" -> Category.SHADOW;
            case "A Reputation Tested" -> Category.COURT;
            case "Marriage Alliance" -> Category.FAMILY;
            case "The First Child Is Born" -> Category.FAMILY;

            // --- Scholar arcs ---
            case "The Circle of the Teacher" -> Category.SCHOLAR;
            case "The Copyist's Wage" -> Category.SCHOLAR;
            case "The Chair Falls Vacant" -> Category.SCHOLAR;
            case "What the Students Carry" -> Category.SCHOLAR;
            case "The Question That Should Not Be Asked" -> Category.SCHOLAR;
            case "The Accusation Is Filed" -> Category.COURT;
            case "The Inquisitor's Season" -> Category.COURT;
            case "The Verdict History Keeps" -> Category.LEGACY;

            // --- Merchant arcs ---
            case "A Share in the Cargo" -> Category.BAZAAR;
            case "The Toll at the Pass" -> Category.EXILE;
            case "The Letter of Credit" -> Category.BAZAAR;
            case "What the House Becomes" -> Category.BAZAAR;
            case "The Guild Closes Its Books" -> Category.BAZAAR;
            case "Fire in the Workshops" -> Category.BAZAAR;
            case "The Court Takes an Interest" -> Category.COURT;

            // --- Military arcs ---
            case "The Oath and the Iron" -> Category.MILITARY;
            case "The Pay Does Not Arrive" -> Category.MILITARY;
            case "The Night the Corps Decides" -> Category.MILITARY;
            case "The Ribat at the Edge" -> Category.MILITARY;
            case "The Siege" -> Category.MILITARY;
            case "What the Frontier Owes You" -> Category.MILITARY;
            case "Taken for the Corps" -> Category.MILITARY;
            case "The Kettle Is Overturned" -> Category.MILITARY;
            case "The Village You Came From" -> Category.COMMONER;

            // --- Court and shadow arcs ---
            case "The Quiet Offer" -> Category.SHADOW;
            case "The Report They Want" -> Category.SHADOW;
            case "The Net Draws In" -> Category.SHADOW;
            case "A Small Favour" -> Category.SHADOW;
            case "The Interest Compounds" -> Category.SHADOW;
            case "What They Ask For Last" -> Category.SHADOW;
            case "The Old Order Thins" -> Category.PALACE;
            case "The Turning" -> Category.CRISIS;
            case "Who Survived, and How" -> Category.LEGACY;

            // --- Commoner arcs ---
            case "The Price of Bread Doubles" -> Category.CRISIS;
            case "The Crowd Finds a Voice" -> Category.CRISIS;
            case "The Reckoning After" -> Category.COURT;
            case "The Assessment Is Wrong" -> Category.COMMONER;
            case "The Collector Returns With Soldiers" -> Category.COMMONER;
            case "The Granary Is Yours to Open" -> Category.CRISIS;
            case "What the Famine Left" -> Category.CRISIS;
            case "The Hand That Lifts You" -> Category.COMMONER;
            case "The Patron Falls" -> Category.COURT;
            case "A Child in a Doorway" -> Category.COMMONER;

            // --- Family arcs ---
            case "The Contract Is Negotiated" -> Category.FAMILY;
            case "Your Spouse's House Wants Something" -> Category.FAMILY;
            case "Forty Years of It" -> Category.FAMILY;
            case "The Estate Is Divided Badly" -> Category.FAMILY;
            case "Blood in the Watercourse" -> Category.FAMILY;
            case "The Quarrel You Hand Down" -> Category.FAMILY;
            case "Which of Them Receives It" -> Category.FAMILY;
            case "The Last Instruction" -> Category.LEGACY;
            case "A Chance to Mentor" -> Category.SCHOLAR;
            case "Debt Comes Due" -> Category.COMMONER;
            case "A Private Offer" -> Category.SHADOW;
            case "A Scholar Asked to Judge" -> Category.COURT;
            case "Merchants Seek Your Protection" -> Category.BAZAAR;
            case "Command Over Restless Soldiers" -> Category.MILITARY;

            case "Rumors of Unrest" -> Category.CRISIS;
            case "Pressure from the Powerful" -> Category.COURT;
            case "The Bread Riot" -> Category.CRISIS;
            case "A Trial in Public" -> Category.COURT;
            case "A Faction Demands Loyalty" -> Category.COURT;
            case "Court Demands Proof of Loyalty" -> Category.COURT;
            case "The Shadow Network Collects" -> Category.SHADOW;

            case "A Question of Legacy" -> Category.LEGACY;
            case "The Last Household Council" -> Category.FAMILY;
            case "The Final Patronage" -> Category.LEGACY;

            case "The Collector Returns" -> Category.BAZAAR;
            case "The Scholar’s Complaint", "The Scholar's Complaint" -> Category.COURT;
            case "The Secret Finds Its Way Back" -> Category.SHADOW;
            case "The Rival’s Revenge", "The Rival's Revenge" -> Category.SHADOW;
            case "The Patron Turns Against You" -> Category.COURT;

            case "The Father's Weakness" -> Category.PALACE;
            case "The Brother's Claim" -> Category.PALACE;
            case "The Throne Opens" -> Category.PALACE;
            case "The Price of the Crown" -> Category.PALACE;

            case "The Stain on the Family Name" -> Category.FAMILY;
            case "The Rival Recites the Past" -> Category.COURT;
            case "The Forgotten Record" -> Category.SCHOLAR;
            case "Honor Before the Court" -> Category.COURT;
            case "The Name They Remember" -> Category.LEGACY;

            case "The Road Back" -> Category.EXILE;
            case "The Gatekeeper's Price" -> Category.EXILE;
            case "The Old Enemy Remembers" -> Category.COURT;
            case "The Court Hears the Exile" -> Category.COURT;
            case "Return Written in Memory" -> Category.LEGACY;

            default -> fallbackCategory(event, player);
        };
    }

    private static Category fallbackCategory(GameEvent event, PlayerCharacter player) {
        String title = lower(event.getTitle());
        String stage = lower(event.getLifeStage());

        if (containsAny(title, "family", "household", "marriage", "inheritance", "bloodline", "council")) {
            return Category.FAMILY;
        }

        if (containsAny(title, "trial", "court", "judgment", "hearing", "accusation")) {
            return Category.COURT;
        }

        if (containsAny(title, "throne", "crown", "palace", "royal", "father", "brother")) {
            return Category.PALACE;
        }

        if (containsAny(title, "scholar", "library", "madrasa", "record", "lesson", "tutor")) {
            return Category.SCHOLAR;
        }

        if (containsAny(title, "merchant", "market", "bazaar", "caravan", "trade", "tax")) {
            return Category.BAZAAR;
        }

        if (containsAny(title, "soldier", "barracks", "military", "frontier", "commander")) {
            return Category.MILITARY;
        }

        if (containsAny(title, "shadow", "secret", "spy", "whisper", "hidden")) {
            return Category.SHADOW;
        }

        if (containsAny(title, "exile", "road", "return", "gate")) {
            return Category.EXILE;
        }

        if (containsAny(title, "riot", "unrest", "famine", "hunger", "bread", "crisis")) {
            return Category.CRISIS;
        }

        if (stage.contains("legacy")) return Category.LEGACY;
        if (stage.contains("childhood")) return Category.COMMONER;

        return getOriginCategory(player);
    }

    private static Category getOriginCategory(PlayerCharacter player) {
        if (player == null) return Category.COMMONER;

        String origin = lower(player.getOrigin());
        String family = lower(player.getFamilyCondition());
        String status = lower(player.getCurrentStatus());

        if (containsAny(status, "ruler", "regent", "court", "noble")) return Category.PALACE;
        if (containsAny(status, "scholar", "educated")) return Category.SCHOLAR;
        if (containsAny(status, "merchant")) return Category.BAZAAR;
        if (containsAny(status, "military", "commander")) return Category.MILITARY;
        if (containsAny(status, "shadow")) return Category.SHADOW;
        if (containsAny(status, "exile", "returnee")) return Category.EXILE;

        if (containsAny(origin, "royal", "noble", "governor")) return Category.PALACE;
        if (containsAny(origin, "scholar", "madrasa", "scribe")) return Category.SCHOLAR;
        if (origin.contains("merchant")) return Category.BAZAAR;
        if (containsAny(origin, "soldier", "military", "mamluk", "janissary")) return Category.MILITARY;
        if (family.contains("exiled")) return Category.EXILE;

        return Category.COMMONER;
    }

    private static List<String> pool(Category category) {
        List<String> selected = switch (category) {
            case MENU -> MENU;
            case BIRTH -> BIRTH;
            case COMMONER -> COMMONER;
            case FAMILY -> FAMILY;
            case PALACE -> PALACE;
            case SCHOLAR -> SCHOLAR;
            case BAZAAR -> BAZAAR;
            case MILITARY -> MILITARY;
            case SHADOW -> SHADOW;
            case EXILE -> EXILE;
            case CRISIS -> CRISIS;
            case COURT -> COURT;
            case LEGACY -> LEGACY;
            case PRISON -> PRISON;
        };

        if (selected != null && !selected.isEmpty()) return selected;

        return switch (category) {
            case COURT -> PALACE;
            case CRISIS -> BAZAAR;
            case PRISON -> SHADOW;
            case FAMILY -> COMMONER;
            default -> ALL_IMAGES;
        };
    }

    private static List<String> loadCategory(String folder) {
        List<String> result = new ArrayList<>();

        try {
            String resourcePath = folder.isEmpty() ? BASE : BASE + folder;
            URL url = BackgroundLibrary.class.getResource(resourcePath);

            if (url == null) {
                System.out.println("Background folder not found: " + resourcePath);
                return result;
            }

            Path rootPath = Paths.get(url.toURI());

            try (Stream<Path> stream = Files.walk(rootPath)) {
                stream
                        .filter(Files::isRegularFile)
                        .filter(BackgroundLibrary::isImageFile)
                        .sorted(Comparator.comparing(Path::toString))
                        .forEach(path -> {
                            Path relativePath = rootPath.relativize(path);
                            String cleanPath = relativePath.toString().replace("\\", "/");

                            String fullPath = folder.isEmpty()
                                    ? BASE + cleanPath
                                    : BASE + folder + "/" + cleanPath;

                            if (isUsableBackground(fullPath)) {
                                result.add(fullPath);
                            }
                        });
            }

            System.out.println("Loaded " + result.size() + " usable backgrounds from: " + resourcePath);

        } catch (Exception e) {
            System.out.println("Failed loading category: " + folder);
            System.out.println(e.getMessage());
        }

        return result;
    }

    private static boolean isImageFile(Path path) {
        String file = path.toString().toLowerCase();
        return file.endsWith(".jpg") || file.endsWith(".jpeg") || file.endsWith(".png");
    }

    private static boolean isUsableBackground(String path) {
        String p = lower(path);

        if (p.contains("/98_misc_uncategorized/")) return false;
        if (p.contains("/99_art_reference/")) return false;

        if (p.contains("portrait")) return false;
        if (p.contains("vertical")) return false;
        if (p.contains("door_")) return false;
        if (p.contains("warrior_")) return false;
        if (p.contains("character_")) return false;
        if (p.contains("coffin")) return false;
        if (p.contains("low_quality")) return false;
        if (p.contains("lowres")) return false;
        if (p.contains("tiny")) return false;
        if (p.contains("logo")) return false;
        if (p.contains("watermark")) return false;

        // Single-subject close-ups that read poorly stretched full-bleed
        // behind the HUD, as opposed to the environment/scene shots this
        // library is built for.
        if (p.contains("princes_")) return false;
        if (p.contains("royal_woman_")) return false;

        return true;
    }

    private static List<String> findPreferredImages(String... parts) {
        List<String> found = new ArrayList<>();

        for (String image : ALL_IMAGES) {
            String img = lower(image);

            for (String part : parts) {
                if (img.contains(lower(part))) {
                    found.add(image);
                    break;
                }
            }
        }

        return found;
    }

    private static String pick(List<String> paths) {
        if (paths == null || paths.isEmpty()) paths = ALL_IMAGES;
        if (paths == null || paths.isEmpty()) return null;

        if (paths.size() == 1) {
            lastBackground = paths.get(0);
            return lastBackground;
        }

        String selected = paths.get(random.nextInt(paths.size()));
        int attempts = 0;

        while (selected.equals(lastBackground) && attempts < 10) {
            selected = paths.get(random.nextInt(paths.size()));
            attempts++;
        }

        lastBackground = selected;
        return selected;
    }

    private static boolean containsAny(String text, String... words) {
        for (String word : words) {
            if (text.contains(word)) return true;
        }
        return false;
    }

    private static String lower(String text) {
        return text == null ? "" : text.toLowerCase();
    }
}