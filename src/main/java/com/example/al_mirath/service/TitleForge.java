package com.example.al_mirath.service;

import com.example.al_mirath.model.EarnedTitle;
import com.example.al_mirath.model.FactionRelations;
import com.example.al_mirath.model.PlayerCharacter;
import com.example.al_mirath.model.WorldState;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds a life's titles instead of drawing them from a fixed list.
 *
 * <p>The game used to hand out roughly two dozen pre-written honorifics: if
 * your wealth and the merchants' regard both cleared a line you became "Golden
 * Hand", the same three words every run. This forges the name from the shape
 * of the life instead. The arena you are known in comes from your strongest
 * faction and your dominant stat; the exact epithet inside that arena is then
 * chosen by a second signal — a trait, a secondary stat, the era's seat of
 * power — so two military heroes can end as "Lion of Cairo" and "The Tigris
 * Wolf", and a life lived under the Umayyads never produces a Cairo title.
 *
 * <p>Deterministic on purpose: the same life-state always forges the same
 * words, so a stat drifting a point or two does not spray the record with
 * near-duplicates.
 */
public final class TitleForge {

    /**
     * Every title the life has earned so far, ordered weakest to strongest.
     *
     * <p>At most one per arena: within an arena the forms are checked in
     * priority order — which identity dominates — not strictly by prestige,
     * so a ruthless general is "The Iron Hand" even though a heroic one would
     * outrank the name.
     */
    public List<EarnedTitle> forge(
            PlayerCharacter player,
            FactionRelations factions,
            WorldState world
    ) {
        List<EarnedTitle> earned = new ArrayList<>();

        addIfPresent(earned, rule(player, factions, world));
        addIfPresent(earned, war(player, factions));
        addIfPresent(earned, judgment(player, factions));
        addIfPresent(earned, scholarship(player, factions));
        addIfPresent(earned, trade(player, factions));
        addIfPresent(earned, faith(player, factions));
        addIfPresent(earned, street(player, factions));
        addIfPresent(earned, shadows(player, factions));
        addIfPresent(earned, blood(player, factions));
        addIfPresent(earned, scars(player));

        earned.sort((a, b) -> a.prestige() - b.prestige());

        return earned;
    }

    /** The one name history leads with, or null when the life earned none. */
    public EarnedTitle crowningTitle(List<EarnedTitle> earned) {
        EarnedTitle crown = null;

        for (EarnedTitle title : earned) {
            if (crown == null || title.prestige() > crown.prestige()) {
                crown = title;
            }
        }

        return crown;
    }

    // ---- the era's furniture -------------------------------------------

    /** The seat of power a life is measured against. */
    static String seatOf(String era, String origin) {
        String home = origin == null ? "" : origin.toLowerCase();

        if (home.contains("baghdad")) {
            return "Baghdad";
        }
        if (home.contains("cairo")) {
            return "Cairo";
        }
        if (home.contains("istanbul")) {
            return "Istanbul";
        }
        if (home.contains("damascus")) {
            return "Damascus";
        }

        return switch (era == null ? "" : era) {
            case "Umayyad Era" -> "Damascus";
            case "Abbasid Era" -> "Baghdad";
            case "Mamluk Era" -> "Cairo";
            case "Ottoman Era" -> "Istanbul";
            default -> "the Capital";
        };
    }

    /** The country the frontier stories attach to. */
    static String regionOf(String era) {
        return switch (era == null ? "" : era) {
            case "Umayyad Era" -> "Desert";
            case "Abbasid Era" -> "Tigris";
            case "Mamluk Era" -> "Nile";
            case "Ottoman Era" -> "Anatolian";
            default -> "Frontier";
        };
    }

    // ---- one arena per method ----------------------------------------

    private EarnedTitle rule(PlayerCharacter p, FactionRelations f, WorldState w) {
        boolean throne = w.hasFlag("took_throne") || w.hasFlag("became_regent");

        // Power alone is common in this game; a life is only named for rule
        // when it went well past the point where most lives stall.
        if (!throne && p.getPoliticalPower() < 80) {
            return null;
        }

        String seat = seatOf(p.getEra(), p.getOrigin());

        if (throne && p.getReputation() >= 60) {
            return new EarnedTitle("Sovereign of " + seat, 100, "RULE");
        }
        if (p.getPoliticalPower() >= 88 && p.getMorality() <= 25) {
            return new EarnedTitle("Breaker of Dynasties", 90, "RULE");
        }
        if (p.getPoliticalPower() >= 82 && p.getEducation() >= 70) {
            return new EarnedTitle("The Crowned Scholar", 87, "RULE");
        }
        if (p.getPoliticalPower() >= 80 && f.getCourt() <= 25) {
            return new EarnedTitle("The Last Crown", 83, "RULE");
        }

        return new EarnedTitle("The Rising Seat", 52, "RULE");
    }

    private EarnedTitle war(PlayerCharacter p, FactionRelations f) {
        if (f.getMilitary() < 65) {
            return null;
        }

        if (p.getMorality() <= 25 && f.getMilitary() >= 75) {
            return new EarnedTitle("The Iron Hand", 84, "WAR");
        }
        if (p.getReputation() >= 60 && p.getHealth() >= 55) {
            return new EarnedTitle("Lion of " + seatOf(p.getEra(), p.getOrigin()), 86, "WAR");
        }
        if (traitIs(p, "Cunning") || p.getEducation() >= 55) {
            return new EarnedTitle("The " + regionOf(p.getEra()) + " Wolf", 78, "WAR");
        }
        if (p.getHealth() >= 65 && p.getPoliticalPower() >= 40) {
            return new EarnedTitle("Shield of the March", 70, "WAR");
        }

        return new EarnedTitle("The Sword Unsheathed", 54, "WAR");
    }

    private EarnedTitle judgment(PlayerCharacter p, FactionRelations f) {
        if (p.getEducation() < 60 || p.getMorality() < 65 || f.getScholars() < 55) {
            return null;
        }

        if (p.getWealth() <= 35 && p.getMorality() >= 80) {
            return new EarnedTitle("The Blind Judge", 85, "LAW");
        }
        if (p.getMorality() >= 85) {
            return new EarnedTitle("The Just Hand", 74, "LAW");
        }

        return new EarnedTitle("Keeper of the Scales", 60, "LAW");
    }

    private EarnedTitle scholarship(PlayerCharacter p, FactionRelations f) {
        if (p.getEducation() < 75 || f.getScholars() < 60) {
            return null;
        }

        if (p.getReputation() >= 55 && p.getPoliticalPower() >= 40) {
            return new EarnedTitle("The Learned Vizier", 85, "SCHOLARSHIP");
        }
        if (p.getEducation() >= 90) {
            return new EarnedTitle("Lamp of the Age", 86, "SCHOLARSHIP");
        }
        if (p.getWealth() >= 60) {
            return new EarnedTitle("The Merchant of Letters", 64, "SCHOLARSHIP");
        }

        return new EarnedTitle("Light of the Madrasa", 58, "SCHOLARSHIP");
    }

    private EarnedTitle trade(PlayerCharacter p, FactionRelations f) {
        if (p.getWealth() < 70 || f.getMerchants() < 60) {
            return null;
        }

        if (f.getMerchants() >= 80) {
            return new EarnedTitle("Master of the Caravan Roads", 88, "TRADE");
        }
        if (p.getStress() >= 70 && p.getHealth() >= 45) {
            return new EarnedTitle("The Iron Merchant", 76, "TRADE");
        }
        if (p.getMorality() <= 30) {
            return new EarnedTitle("The Golden Serpent", 70, "TRADE");
        }

        return new EarnedTitle("The Golden Hand", 62, "TRADE");
    }

    private EarnedTitle faith(PlayerCharacter p, FactionRelations f) {
        boolean followed = f.getScholars() >= 55 || f.getCommonPeople() >= 60;

        if (p.getMorality() < 75 || p.getReputation() < 55 || !followed) {
            return null;
        }

        if (p.getWealth() <= 25) {
            return new EarnedTitle("The Barefoot Preacher", 80, "FAITH");
        }
        if (p.getEducation() >= 70) {
            return new EarnedTitle("Voice of the Friday Mosque", 68, "FAITH");
        }

        return new EarnedTitle("The Devout", 52, "FAITH");
    }

    private EarnedTitle street(PlayerCharacter p, FactionRelations f) {
        if (f.getCommonPeople() < 70) {
            return null;
        }

        if (p.getWealth() <= 20 && p.getReputation() >= 55) {
            return new EarnedTitle("Voice of the Poor", 83, "STREET");
        }
        if (p.getReputation() >= 60 && p.getHealth() >= 55) {
            return new EarnedTitle("Hero of the Alleys", 72, "STREET");
        }
        if (p.getMorality() >= 65) {
            return new EarnedTitle("The Open Hand", 56, "STREET");
        }

        return new EarnedTitle("Friend of the Crowd", 48, "STREET");
    }

    private EarnedTitle shadows(PlayerCharacter p, FactionRelations f) {
        if (f.getShadowNetwork() < 65) {
            return null;
        }

        if (p.getReputation() <= 30 && f.getShadowNetwork() >= 80) {
            return new EarnedTitle("The Unseen Hand", 84, "SHADOWS");
        }
        if (p.getMorality() <= 25) {
            return new EarnedTitle("The Knife in the Dark", 74, "SHADOWS");
        }

        return new EarnedTitle("Keeper of Secrets", 54, "SHADOWS");
    }

    private EarnedTitle blood(PlayerCharacter p, FactionRelations f) {
        if (p.getFamilyLoyalty() < 75) {
            return null;
        }

        String condition = p.getFamilyCondition() == null ? "" : p.getFamilyCondition();
        boolean fallenHouse =
                condition.equals("Disgraced Bloodline")
                        || condition.equals("Exiled Branch");

        if (fallenHouse && p.getReputation() >= 60) {
            return new EarnedTitle("Restorer of the Name", 82, "BLOOD");
        }
        if (p.getMorality() <= 30 && p.getFamilyLoyalty() >= 82) {
            return new EarnedTitle("The Iron Root of the House", 64, "BLOOD");
        }
        if (p.getMorality() >= 60) {
            return new EarnedTitle("Pillar of the Bloodline", 66, "BLOOD");
        }

        return new EarnedTitle("Keeper of the Hearth", 46, "BLOOD");
    }

    private EarnedTitle scars(PlayerCharacter p) {
        if (p.getStress() < 85 && p.getHealth() > 20) {
            return null;
        }

        String condition = p.getFamilyCondition() == null ? "" : p.getFamilyCondition();

        if (condition.equals("Recently Orphaned") && p.getReputation() >= 55) {
            return new EarnedTitle("Orphan of Iron", 56, "SCARS");
        }
        if (p.getStress() >= 90 && p.getReputation() >= 45) {
            return new EarnedTitle("The Unbroken", 58, "SCARS");
        }
        if (p.getHealth() <= 20 && p.getReputation() >= 60) {
            return new EarnedTitle("The Fading Flame", 55, "SCARS");
        }

        return null;
    }

    private static void addIfPresent(List<EarnedTitle> list, EarnedTitle title) {
        if (title != null) {
            list.add(title);
        }
    }

    private static boolean traitIs(PlayerCharacter p, String trait) {
        return p.getTrait() != null && p.getTrait().equalsIgnoreCase(trait);
    }
}
