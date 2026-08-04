package com.example.al_mirath.service;

import com.example.al_mirath.model.WorldEvent;

import java.util.List;
import java.util.Map;

/**
 * The pool of ancient-decree world announcements.
 *
 * <p>Most entries carry no allowed-era list, meaning any era may draw them;
 * a handful are written for a specific era's politics and are restricted to
 * it. Every entry causes a small, deliberately modest shift to stats or
 * faction standing — modest because these fire independently of the player's
 * own choices roughly one time in five, and should color a life rather than
 * dominate it.
 */
public final class WorldEventLibrary {

    private static final List<WorldEvent> EVENTS = List.of(
            new WorldEvent(
                    "The Ruler Has Died",
                    "Bells and criers carry the news through every quarter of the city: the "
                            + "ruler is dead. Markets close early. Soldiers double their watch. No one "
                            + "yet knows who will hold power when the sun rises tomorrow.",
                    List.of(),
                    Map.of("stress", 6),
                    Map.of("court", -8)
            ),
            new WorldEvent(
                    "Rebellion Ignites in the Provinces",
                    "Word arrives of an uprising in the outer provinces. Tax collectors have "
                            + "been driven out, a local commander has declared defiance, and the court "
                            + "argues over whether to answer with soldiers or with concessions.",
                    List.of(),
                    Map.of("stress", 5),
                    Map.of("court", -5, "commonPeople", 4)
            ),
            new WorldEvent(
                    "A Great Plague Spreads",
                    "Illness moves through the crowded streets faster than any decree can "
                            + "contain it. Physicians run short of remedies. Some households shut "
                            + "their doors to the world; others turn to prayer, and some to panic.",
                    List.of(),
                    Map.of("health", -8, "stress", 6),
                    Map.of()
            ),
            new WorldEvent(
                    "A New Trade Route Opens",
                    "Caravans return with news of a route reopened after years of danger. "
                            + "Merchants speak of silk, spice, and coin flowing freely again. "
                            + "The bazaars grow louder within days.",
                    List.of(),
                    Map.of("wealth", 6),
                    Map.of("merchants", 6)
            ),
            new WorldEvent(
                    "Famine Grips the Land",
                    "A poor harvest has become a crisis. Grain prices climb by the week, "
                            + "and the poor are the first to feel it. Granaries once thought "
                            + "bottomless are measured now in careful handfuls.",
                    List.of(),
                    Map.of("wealth", -6, "stress", 5),
                    Map.of("commonPeople", -5)
            ),
            new WorldEvent(
                    "A Foreign Army Gathers at the Border",
                    "Riders bring warning of an army massing beyond the frontier. Whether it "
                            + "means invasion, a show of strength, or a threat meant only for "
                            + "negotiation, no one in the city can yet say.",
                    List.of(),
                    Map.of("stress", 6),
                    Map.of("military", 5)
            ),
            new WorldEvent(
                    "A Comet Is Seen as an Omen",
                    "A pale light crosses the night sky for three nights running. Scholars "
                            + "argue over its cause. The common people are less patient with "
                            + "explanations — to them, it is a sign, and signs demand meaning.",
                    List.of(),
                    Map.of("stress", 3),
                    Map.of("scholars", 3)
            ),
            new WorldEvent(
                    "Peace Is Declared After Years of War",
                    "A treaty is signed, ending a conflict that outlasted the memory of many "
                            + "who lived through it. Some celebrate in the streets. Others, who lost "
                            + "sons and brothers to the fighting, find little comfort in parchment.",
                    List.of(),
                    Map.of("stress", -8),
                    Map.of("court", 5, "commonPeople", 5)
            ),
            new WorldEvent(
                    "A New Madrasa Opens Its Doors",
                    "A wealthy patron has endowed a new house of learning. Scholars travel "
                            + "from distant cities to teach and to argue, and for a season the "
                            + "streets nearby fill with students carrying more questions than coin.",
                    List.of(),
                    Map.of("education", 3),
                    Map.of("scholars", 6)
            ),
            new WorldEvent(
                    "The Shadow Network Grows Bolder",
                    "Whispers travel faster than official word these days. Informants, "
                            + "smugglers, and hidden brokers seem to know a ruler's plans before "
                            + "the ruler has finished making them.",
                    List.of(),
                    Map.of(),
                    Map.of("shadowNetwork", 6, "court", -3)
            ),
            new WorldEvent(
                    "A Palace Conspiracy Is Uncovered",
                    "Guards seize a household accused of plotting against the throne. Some "
                            + "call it justice. Others whisper that the accused simply lost a game "
                            + "they never knew they were playing.",
                    List.of(),
                    Map.of("stress", 4),
                    Map.of("court", 4, "nobles", -4)
            ),
            new WorldEvent(
                    "The Umayyad Court Consolidates Power",
                    "Governors across the provinces receive new instructions from the court, "
                            + "tightening the reach of Damascus into matters once left to local rule.",
                    List.of("Umayyad Era"),
                    Map.of(),
                    Map.of("court", 6, "nobles", -3)
            ),
            new WorldEvent(
                    "The House of Wisdom Flourishes",
                    "Translators and scholars in Baghdad complete another great work, carrying "
                            + "knowledge from distant tongues into Arabic for the first time. The "
                            + "caliph's court speaks of the city as the center of the learned world.",
                    List.of("Abbasid Era"),
                    Map.of("education", 2),
                    Map.of("scholars", 6)
            ),
            new WorldEvent(
                    "The Mamluk Garrisons Are Reinforced",
                    "Fresh companies of Mamluk soldiers arrive from the training barracks, "
                            + "sent to strengthen the frontier against raiders who have grown bold.",
                    List.of("Mamluk Era"),
                    Map.of(),
                    Map.of("military", 6)
            ),
            new WorldEvent(
                    "The Sultan's Fleet Returns Victorious",
                    "Ships return to harbor flying banners of victory. Word spreads of a naval "
                            + "engagement won at great cost, and the city prepares a celebration "
                            + "that will be remembered for years.",
                    List.of("Ottoman Era"),
                    Map.of("stress", -4),
                    Map.of("military", 5, "commonPeople", 4)
            )
    );

    private WorldEventLibrary() {
    }

    public static List<WorldEvent> all() {
        return EVENTS;
    }
}
