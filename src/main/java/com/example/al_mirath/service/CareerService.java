package com.example.al_mirath.service;

import com.example.al_mirath.model.Career;
import com.example.al_mirath.model.City;
import com.example.al_mirath.model.FactionRelations;
import com.example.al_mirath.model.PlayerCharacter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Getting a trade, keeping it, and climbing it.
 *
 * <p>A career is the difference between a life that has a shape and a list of
 * afternoons. It pays every year without being asked, it gates half the
 * interesting activities, and it is the one thing in the game that rewards
 * staying put: rank is bought with years as much as with stats.
 *
 * <p>Nothing here touches the world directly — every method returns what
 * happened and leaves {@link GameEngine} to apply it and write the log.
 */
public final class CareerService {

    private static final Random RANDOM = new Random();

    private CareerService() {
    }

    /**
     * A post the character could be taken on for.
     *
     * @param career     the trade
     * @param entryRank  the rung they would start on
     * @param trial      the mini-game the interview runs, or null when the
     *                   post is open to anyone who turns up
     * @param blockedBy  why they cannot apply, or empty when they can
     */
    public record Opening(Career career, int entryRank, String trial, String blockedBy) {

        public boolean isOpen() {
            return blockedBy.isEmpty();
        }

        public int startingPay() {
            return career.incomeAt(entryRank);
        }

        public String title() {
            String rank = career.rank(entryRank).title();

            // "Physician, Physician" reads like a stutter. Where the rung and
            // the trade share a name, the name is enough.
            if (rank.equalsIgnoreCase(career.displayName())) {
                return rank;
            }

            return rank + ", " + career.displayName();
        }
    }

    /** What a hopeful is tested on, by trade. */
    private static String trialFor(Career career) {
        return switch (career) {
            case SCHOLAR -> "scribe";
            case MERCHANT -> "haggle";
            case SOLDIER -> "archery";
            case COURTIER -> "orator";
            case PHYSICIAN -> "physician";
            case IMAM -> "prosody";
            case CRIMINAL -> "lighthand";
            case ARTISAN -> "geometer";
            case FARMER, UNEMPLOYED -> null;
        };
    }

    /** Stat floors to be taken seriously at all. */
    private static Map<String, Integer> entryStats(Career career) {
        return switch (career) {
            case FARMER -> Map.of();
            case ARTISAN -> Map.of("education", 15);
            case SCHOLAR -> Map.of("education", 35);
            case MERCHANT -> Map.of("wealth", 25);
            case SOLDIER -> Map.of("health", 40);
            case COURTIER -> Map.of("politicalPower", 25, "reputation", 30);
            case PHYSICIAN -> Map.of("education", 45);
            case IMAM -> Map.of("morality", 45, "education", 30);
            case CRIMINAL -> Map.of();
            case UNEMPLOYED -> Map.of();
        };
    }

    /** Whose good opinion you need before anyone will hear you out. */
    private static Map<String, Integer> entryFactions(Career career) {
        return switch (career) {
            case SCHOLAR -> Map.of("scholars", 30);
            case MERCHANT -> Map.of("merchants", 30);
            case SOLDIER -> Map.of("military", 30);
            case COURTIER -> Map.of("court", 35);
            case IMAM -> Map.of("commonPeople", 30);
            case CRIMINAL -> Map.of("shadowNetwork", 20);
            default -> Map.of();
        };
    }

    /**
     * Every trade the character could walk into today, with the reason on any
     * they could not.
     *
     * <p>Unreachable posts are listed rather than hidden, because "Needs
     * Scholars 30" is a plan and a missing row is nothing.
     */
    public static List<Opening> openings(PlayerCharacter player,
                                         FactionRelations factions,
                                         City city) {

        List<Opening> openings = new ArrayList<>();

        for (Career career : Career.values()) {
            if (career == Career.UNEMPLOYED) {
                continue;
            }

            openings.add(openingFor(career, player, factions, city));
        }

        return openings;
    }

    private static Opening openingFor(Career career,
                                      PlayerCharacter player,
                                      FactionRelations factions,
                                      City city) {

        String blocked = "";

        if (player.getAge() < career.minimumAge()) {
            blocked = "Not until you are " + career.minimumAge() + ".";
        } else if (player.getCareer() == career && !player.isRetired()) {
            blocked = "You already hold this.";
        } else {
            for (Map.Entry<String, Integer> need : entryStats(career).entrySet()) {
                if (player.getStatValue(need.getKey()) < need.getValue()) {
                    blocked = "Needs " + label(need.getKey()) + " " + need.getValue() + ".";
                    break;
                }
            }

            if (blocked.isEmpty() && factions != null) {
                for (Map.Entry<String, Integer> need : entryFactions(career).entrySet()) {
                    if (factions.getValue(need.getKey()) < adjustedFactionFloor(need, city)) {
                        blocked = "Needs the standing of the "
                                + label(need.getKey()).toLowerCase() + ".";
                        break;
                    }
                }
            }
        }

        return new Opening(
                career,
                entryRankFor(career, player),
                trialFor(career),
                blocked
        );
    }

    /**
     * A city bends who it is hiring.
     *
     * <p>A city built on its madrasas will take on a copyist it would turn
     * away elsewhere, and a city on a trade road will find a place for anyone
     * who can count. This is most of what travelling is for.
     */
    private static int adjustedFactionFloor(Map.Entry<String, Integer> need, City city) {
        if (city == null) {
            return need.getValue();
        }

        int relief = switch (need.getKey()) {
            case "scholars" -> city.getScholarship() >= 65 ? 10 : 0;
            case "merchants" -> city.getTrade() >= 65 ? 10 : 0;
            case "shadowNetwork" -> city.getCrime() >= 60 ? 8 : 0;
            case "military" -> city.getWar() >= 50 ? 10 : 0;
            default -> 0;
        };

        return Math.max(0, need.getValue() - relief);
    }

    /** The highest rung they already qualify for, so talent is not wasted. */
    private static int entryRankFor(Career career, PlayerCharacter player) {
        return career.entryRank(player::getStatValue);
    }

    // ── Advancement ─────────────────────────────────────────────────────

    /** Years that must be served on a rung before the next is even discussed. */
    private static final int YEARS_BEFORE_ADVANCEMENT = 3;

    /**
     * Whether a promotion is due this year, on time served and merit both.
     *
     * <p>Merit alone would promote a brilliant twenty-year-old to vizier; time
     * alone would promote anyone who simply lived. It takes both.
     */
    public static boolean isPromotionDue(PlayerCharacter player) {
        if (!player.isEmployed()) {
            return false;
        }

        if (player.getYearsInRank() < YEARS_BEFORE_ADVANCEMENT) {
            return false;
        }

        return player.getCareer().canPromote(player.getCareerRank(), player::getStatValue);
    }

    /** What is standing between the character and the next rung. */
    public static String nextRungRequirement(PlayerCharacter player) {
        Career career = player.getCareer();
        int next = player.getCareerRank() + 1;

        if (!player.isEmployed()) {
            return "You hold no post.";
        }

        if (next > career.maxRank()) {
            return "There is nothing above you in this trade.";
        }

        StringBuilder wanted = new StringBuilder();

        for (Map.Entry<String, Integer> need : career.rank(next).requirements().entrySet()) {
            if (player.getStatValue(need.getKey()) < need.getValue()) {
                if (!wanted.isEmpty()) {
                    wanted.append(", ");
                }

                wanted.append(label(need.getKey()))
                        .append(" ")
                        .append(need.getValue());
            }
        }

        int yearsShort = YEARS_BEFORE_ADVANCEMENT - player.getYearsInRank();

        if (wanted.isEmpty() && yearsShort > 0) {
            return "You are ready. They want to see another "
                    + yearsShort + (yearsShort == 1 ? " year" : " years") + " of it.";
        }

        if (wanted.isEmpty()) {
            return "You are ready for " + career.rank(next).title() + ".";
        }

        return career.rank(next).title() + " wants " + wanted + ".";
    }

    /**
     * Whether the year goes badly enough to cost the character their post.
     *
     * <p>Only a genuine collapse does it: a standing in ruins, or a body that
     * can no longer do the work. Losing a trade should always be traceable to
     * something the player watched happen.
     */
    public static boolean isDismissalDue(PlayerCharacter player, FactionRelations factions) {
        if (!player.isEmployed()) {
            return false;
        }

        Career career = player.getCareer();

        boolean disgraced = player.getReputation() <= 12;
        boolean unfit = player.getHealth() <= 18 && career == Career.SOLDIER;
        boolean disowned = factions != null
                && !entryFactions(career).isEmpty()
                && entryFactions(career).keySet().stream()
                        .allMatch(faction -> factions.getValue(faction) <= 12);

        if (!disgraced && !unfit && !disowned) {
            return false;
        }

        return RANDOM.nextInt(100) < 45;
    }

    /** The line the log gets when a post is lost. */
    public static String dismissalNote(PlayerCharacter player) {
        return switch (player.getCareer()) {
            case SOLDIER -> "You were struck off the muster roll. Nobody made a speech about it.";
            case SCHOLAR -> "Your name came off the teaching list between one term and the next.";
            case COURTIER -> "Your seal was asked for, politely, by somebody junior to you.";
            case MERCHANT -> "The guild stopped putting work your way, which is how they say it.";
            case IMAM -> "They found somebody else to lead the prayer, and did not explain.";
            case PHYSICIAN -> "Patients stopped being sent to you. Then they stopped coming at all.";
            case CRIMINAL -> "You were cut out. In that trade, being cut out is the gentle version.";
            default -> "You were let go.";
        };
    }

    /** The line the log gets on a promotion. */
    public static String promotionNote(PlayerCharacter player) {
        return "You were raised to " + player.getCareer().rank(player.getCareerRank()).title()
                + ". The pay is now " + player.annualIncome() + " dirhams a year.";
    }

    /**
     * Asking outright, rather than waiting to be noticed.
     *
     * <p>Asking early is not free: it costs standing with whoever had to say
     * no, which is the only thing that stops a player asking every year.
     */
    public static String askOutcome(PlayerCharacter player, boolean granted) {
        if (granted) {
            return "You asked, and they had already decided. "
                    + promotionNote(player);
        }

        return "You asked before they were ready to be asked. "
                + nextRungRequirement(player);
    }

    private static String label(String key) {
        return switch (key) {
            case "politicalPower" -> "Influence";
            case "familyLoyalty" -> "Family";
            case "commonPeople" -> "Common People";
            case "shadowNetwork" -> "Shadow Network";
            default -> key.substring(0, 1).toUpperCase() + key.substring(1);
        };
    }
}
