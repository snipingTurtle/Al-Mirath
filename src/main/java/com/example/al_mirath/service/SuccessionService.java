package com.example.al_mirath.service;

import com.example.al_mirath.model.FactionRelations;
import com.example.al_mirath.model.FamilyMember;
import com.example.al_mirath.model.Kinship;
import com.example.al_mirath.model.LifePath;
import com.example.al_mirath.model.PlayerCharacter;
import com.example.al_mirath.model.RecurringCharacter;
import com.example.al_mirath.model.RelationshipType;
import com.example.al_mirath.model.Succession;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Who carries the house next, and what they get when they do.
 *
 * <p>The rule the whole feature turns on is what crosses the boundary. Nothing
 * personal does: the heir's health, learning and nerve are their own, earned
 * from scratch. What crosses is everything the world holds rather than the
 * body — the money, the family's standing, the cities, and the name people
 * already say when they hear which house you are from.
 *
 * <p>A student inherits a different half of that than a child does. Blood gets
 * the money and the position; a chosen successor gets the learning and has to
 * find the rest themselves.
 */
public final class SuccessionService {

    /** How much of a fortune survives division, debts and the funeral. */
    private static final double WEALTH_TO_BLOOD = 0.5;
    private static final double WEALTH_TO_STUDENT = 0.2;

    /** A house's name opens doors for an heir, but not as wide. */
    private static final double REPUTATION_CARRIED = 0.4;
    private static final double POWER_TO_BLOOD = 0.3;
    private static final double POWER_TO_STUDENT = 0.15;

    /** Factions remember the house, at half the strength of their old feeling. */
    private static final int NEUTRAL_REGARD = 40;

    /**
     * How cold a student can go and still be handed the house. Past this they
     * have stopped being yours in any sense that matters — the cast reclassifies
     * anybody this hostile as a rival, so the type check usually catches it
     * first; this holds the line for warmth a save can hold on its own.
     */
    private static final int TURNED_AGAINST_YOU = -15;

    private static final int BASE_HEALTH = 60;
    private static final int BASE_MORALITY = 50;
    private static final int BASE_STRESS = 20;

    private SuccessionService() {
    }

    // ---- who ------------------------------------------------------------

    /**
     * Everyone who could take the name, best claim first.
     *
     * <p>Blood before loyalty, and among blood the eldest who is old enough to
     * be handed a house rather than raised into one.
     */
    public static List<Succession> candidates(
            FamilyRegistry family,
            RecurringCharacterRegistry cast
    ) {
        List<Succession> heirs = new ArrayList<>();

        if (family != null) {
            List<FamilyMember> blood = new ArrayList<>();

            for (FamilyMember member : family.all()) {
                if (member.isAlive()
                        && member.getKinship().isDescendant()
                        && member.getAge() >= Succession.OLD_ENOUGH) {

                    blood.add(member);
                }
            }

            blood.sort((a, b) -> b.getAge() - a.getAge());

            for (FamilyMember member : blood) {
                heirs.add(new Succession(
                        member.getId(),
                        member.getName(),
                        "your " + member.getKinship().displayName().toLowerCase(),
                        member.getAge(),
                        member.getTrait(),
                        member.getLifePath(),
                        true
                ));
            }
        }

        // A house with no blood left can still be handed on, which is the
        // difference between a line ending and a line changing hands.
        //
        // It has to be somebody the player deliberately took on, though. Being
        // fond of you is not the same as having been taught by you, and while
        // any warm friend could inherit, the most consequential relationship
        // in a run was a side effect of being liked rather than a decision.
        // Taking a student is an event now, and this is what it is for.
        if (heirs.isEmpty() && cast != null) {
            for (RecurringCharacter character : cast.all()) {
                if (!character.isAlive()
                        || character.getRelationshipType() != RelationshipType.STUDENT
                        || character.getRelationship() <= TURNED_AGAINST_YOU) {

                    continue;
                }

                heirs.add(new Succession(
                        character.getId(),
                        character.getName(),
                        "your student",
                        Math.max(Succession.OLD_ENOUGH, character.getAge()),
                        "devoted to what you taught them",
                        LifePath.UNDECIDED,
                        false
                ));

                if (heirs.size() >= 2) {
                    break;
                }
            }
        }

        return heirs;
    }

    // ---- what -----------------------------------------------------------

    /**
     * Builds the heir as a character in their own right.
     *
     * <p>Health, education and nerve start fresh — being born to a great
     * scholar does not make you one. What the forebear leaves is money, a name
     * and, if they held one, part of a position.
     */
    public static PlayerCharacter heirOf(
            Succession heir,
            PlayerCharacter forebear,
            FamilyRegistry family,
            Random random
    ) {
        int inheritedWealth = (int) Math.round(
                forebear.getWealth() * (heir.blood() ? WEALTH_TO_BLOOD : WEALTH_TO_STUDENT)
        );

        // A well-off, lettered house teaches its children, whoever they are.
        int schooling =
                20
                        + forebear.getEducation() / 4
                        + forebear.getWealth() / 6
                        + (heir.blood() ? 0 : 15);

        int standing = (int) Math.round(forebear.getReputation() * REPUTATION_CARRIED);

        int position = (int) Math.round(
                forebear.getPoliticalPower() * (heir.blood() ? POWER_TO_BLOOD : POWER_TO_STUDENT)
        );

        // How the heir felt about the person they are replacing.
        FamilyMember asChild = family == null ? null : family.get(heir.id());
        int loyalty = asChild == null ? 50 : 40 + asChild.getAffection() / 3;

        return new PlayerCharacter(
                heir.name(),
                forebear.getEra(),
                originFor(heir, forebear),
                conditionFor(forebear),
                heir.trait(),
                heir.age(),
                BASE_HEALTH + random.nextInt(21) - 10,
                inheritedWealth,
                schooling + random.nextInt(11) - 5,
                standing,
                position,
                BASE_MORALITY + forebear.getMorality() / 6 + random.nextInt(11) - 5,
                loyalty,
                BASE_STRESS + random.nextInt(11)
        );
    }

    /** Where the heir starts from, in the words the rest of the game uses. */
    private static String originFor(Succession heir, PlayerCharacter forebear) {
        if (!heir.blood()) {
            return "Student of " + forebear.getName();
        }

        return switch (heir.path() == null ? LifePath.UNDECIDED : heir.path()) {
            case SCHOLAR -> "Scholar's Child";
            case MERCHANT -> "Baghdad Merchant's Child";
            case SOLDIER -> "Frontier Soldier's Child";
            case COURTIER -> "Court Servant";
            case CRIMINAL -> "Poor Urban Child";
            case RULER -> "Provincial Governor's Child";
            case UNDECIDED -> forebear.getOrigin();
        };
    }

    /**
     * What the house is like to be born into now, judged by what the forebear
     * left behind rather than by what they started with.
     */
    private static String conditionFor(PlayerCharacter forebear) {
        if (forebear.getReputation() >= 65 && forebear.getWealth() >= 55) {
            return "Favored by Local Scholars";
        }
        if (forebear.getReputation() <= 25) {
            return "Disgraced Bloodline";
        }
        if (forebear.getWealth() <= 20) {
            return "Merchant Family in Debt";
        }
        if (forebear.getPoliticalPower() >= 70) {
            return "Secret Noble Blood";
        }

        return "Stable Merchant Family";
    }

    /**
     * The factions remember the house, at half the strength of the feeling the
     * forebear earned. Being adored is worth something to your children; it is
     * not worth everything.
     */
    public static FactionRelations regardInheritedFrom(FactionRelations earned) {
        if (earned == null) {
            return new FactionRelations();
        }

        return new FactionRelations(
                halvedToward(earned.getCourt()),
                halvedToward(earned.getNobles()),
                halvedToward(earned.getMilitary()),
                halvedToward(earned.getScholars()),
                halvedToward(earned.getMerchants()),
                halvedToward(earned.getCommonPeople()),
                halvedToward(earned.getFamilyCouncil()),
                halvedToward(earned.getShadowNetwork())
        );
    }

    private static int halvedToward(int regard) {
        return NEUTRAL_REGARD + (regard - NEUTRAL_REGARD) / 2;
    }

    /** The life stage a life beginning at this age belongs in. */
    public static int stageIndexForAge(int age) {
        if (age < 13) {
            return 0;
        }
        if (age < 25) {
            return 1;
        }
        if (age < 45) {
            return 2;
        }

        return 3;
    }

    /** Which kinship the heir's remaining siblings hold to them. */
    public static Kinship siblingKinship() {
        return Kinship.SIBLING;
    }
}
