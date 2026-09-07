package com.example.al_mirath.service;

import com.example.al_mirath.model.Choice;
import com.example.al_mirath.model.GameEvent;
import com.example.al_mirath.model.PlayerCharacter;
import com.example.al_mirath.model.WorldState;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The decision to teach somebody.
 *
 * <p>A marriage happens to you and a child arrives whether you planned one or
 * not; the household announces both and the run carries on. A student is the
 * one attachment of that weight the player actually chooses, and until now it
 * was not a choice at all — anybody warm enough could be handed the house at
 * the end, which meant the most consequential relationship in a run was a
 * side effect of being liked.
 *
 * <p>So it is an event now, offered twice at most in a life: once in the years
 * when the player has something worth learning, and once more at the end, when
 * the alternative is that it dies with them.
 */
public final class StudentEvents {

    /** Set when the player has already turned somebody away once. */
    public static final String REFUSED_ONCE = "refused_a_student";

    /** Set when they have turned the second one away too. That is the last. */
    public static final String REFUSED_TWICE = "refused_a_student_twice";

    /**
     * How accomplished the player has to be before anybody asks. Nobody
     * apprentices themselves to a person with nothing to hand on, so the
     * offer is earned rather than scheduled.
     */
    private static final int WORTH_LEARNING_FROM = 55;

    private StudentEvents() {
    }

    public static List<GameEvent> create(
            RecurringCharacterRegistry cast,
            PlayerCharacter player,
            WorldState worldState
    ) {
        List<GameEvent> events = new ArrayList<>();

        if (cast == null || player == null || cast.hasLivingStudent()) {
            return events;
        }

        boolean refusedOnce = worldState != null && worldState.hasFlag(REFUSED_ONCE);
        boolean refusedTwice = worldState != null && worldState.hasFlag(REFUSED_TWICE);

        if (!hasSomethingToTeach(player)) {
            return events;
        }

        if (!refusedOnce) {
            events.add(atTheDoor(cast.prospectiveStudentName(seedFor(player, 0)), player));
        }

        if (!refusedTwice) {
            events.add(lateOffer(cast.prospectiveStudentName(seedFor(player, 1))));
        }

        return events;
    }

    /** Learning, standing or office — any of the three is worth following. */
    private static boolean hasSomethingToTeach(PlayerCharacter player) {
        return player.getEducation() >= WORTH_LEARNING_FROM
                || player.getReputation() >= WORTH_LEARNING_FROM
                || player.getPoliticalPower() >= WORTH_LEARNING_FROM;
    }

    /**
     * The seed the candidate's name is drawn from.
     *
     * <p>Fixed to the life rather than rolled, so the same person is still at
     * the door the next time the engine builds this event. The offset gives
     * the second offer a different person from the first.
     */
    private static long seedFor(PlayerCharacter player, int offset) {
        String name = player.getName() == null ? "" : player.getName();
        String era = player.getEra() == null ? "" : player.getEra();

        return name.hashCode() * 31L + era.hashCode() + offset * 7919L;
    }

    // ---- the offer -------------------------------------------------------

    private static GameEvent atTheDoor(String candidate, PlayerCharacter player) {
        return new GameEvent(
                candidate + " Asks to Be Taught",

                candidate
                        + " has been waiting outside since before the heat came "
                        + "up, and does not leave when told to. They are young "
                        + "enough that nobody has decided anything about them "
                        + "yet, and they have come to ask you — not the house, "
                        + "not your name, you — to teach them what you know.\n\n"
                        + "It is not a small thing to say yes to. A student eats, "
                        + "takes up hours you do not have, and repeats you in "
                        + "rooms you will never enter. Everything you got wrong "
                        + "goes with them.\n\n"
                        + "But nothing you know outlives you on its own.",

                "Adulthood",

                List.of(
                        new Choice(
                                "Take " + candidate + " as your student",

                                "You say yes, and the shape of your days changes "
                                        + "that week. "
                                        + candidate
                                        + " is slow at first and then suddenly is "
                                        + "not, and you catch yourself explaining "
                                        + "things you had never had to put into "
                                        + "words. People begin to speak of you as "
                                        + "somebody with a student, which is a "
                                        + "different kind of person entirely.",

                                Map.of(
                                        "wealth", -10,
                                        "stress", 6,
                                        "reputation", 6,
                                        "morality", 5,
                                        "education", 3
                                ),

                                Map.of("scholars", 10, "commonPeople", 5),

                                List.of(),

                                List.of(RecurringCharacterRegistry.TAKE_STUDENT_PREFIX + candidate)
                        ),

                        new Choice(
                                "Set them a problem first, and decide by the answer",

                                "education",

                                55,

                                "You give "
                                        + candidate
                                        + " something you struggled with at their "
                                        + "age and leave them to it. What comes "
                                        + "back is not the answer you had, and is "
                                        + "better. You take them on the same day, "
                                        + "and tell nobody why you are pleased.",

                                candidate
                                        + " cannot do it, and you cannot explain it "
                                        + "well enough for them to see why. You "
                                        + "send them off kindly, which does not "
                                        + "make it kind, and afterwards you are "
                                        + "not sure the failure was theirs.",

                                Map.of(
                                        "wealth", -10,
                                        "stress", 4,
                                        "reputation", 10,
                                        "education", 4
                                ),

                                Map.of("stress", 6, "reputation", -3, "morality", -3),

                                Map.of("scholars", 14),

                                Map.of("scholars", -6),

                                List.of(),

                                List.of(RecurringCharacterRegistry.TAKE_STUDENT_PREFIX + candidate),

                                List.of(REFUSED_ONCE)
                        ),

                        new Choice(
                                "Send them away — you carry enough already",

                                "You tell "
                                        + candidate
                                        + " to find somebody with more time, and "
                                        + "mean it as advice. They take it as a "
                                        + "verdict and do not come back. The week "
                                        + "is easier and the house is quieter than "
                                        + "you wanted it to be.",

                                Map.of("stress", -6, "reputation", -3),

                                Map.of("scholars", -8),

                                List.of(),

                                List.of(REFUSED_ONCE)
                        )
                )
        );
    }

    private static GameEvent lateOffer(String candidate) {
        return new GameEvent(
                "What You Know, and Who Is Left to Take It",

                candidate
                        + " is at the door this time, sent by somebody who "
                        + "remembers what you were. You are old enough now that "
                        + "the question has an edge on it: everything you "
                        + "learned the hard way is currently stored in one "
                        + "failing body, and there is no copy.\n\n"
                        + "Late is not the same as too late, but it is close "
                        + "enough that you can see it from here.",

                "Legacy",

                List.of(
                        new Choice(
                                "Take " + candidate + " on, late as it is",

                                "You teach badly at first, out of practice at being "
                                        + "patient, and then better. "
                                        + candidate
                                        + " writes things down that you had never "
                                        + "thought worth writing. Whatever happens "
                                        + "to you now, some of it is somewhere "
                                        + "else as well.",

                                Map.of(
                                        "wealth", -8,
                                        "stress", 5,
                                        "reputation", 8,
                                        "morality", 6
                                ),

                                Map.of("scholars", 12, "commonPeople", 6),

                                List.of(),

                                List.of(RecurringCharacterRegistry.TAKE_STUDENT_PREFIX + candidate)
                        ),

                        new Choice(
                                "Let it die with you",

                                "You send "
                                        + candidate
                                        + " away and tell yourself the work speaks "
                                        + "for itself. It does, for a while. Then "
                                        + "the people who could read it are gone "
                                        + "too, and it is only paper.",

                                Map.of("stress", 4, "morality", -4, "reputation", -4),

                                Map.of("scholars", -10),

                                List.of(),

                                List.of(REFUSED_TWICE)
                        )
                )
        );
    }
}
