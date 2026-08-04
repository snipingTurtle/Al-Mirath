package com.example.al_mirath.service;

import com.example.al_mirath.model.Choice;
import com.example.al_mirath.model.ChoiceRequirement;
import com.example.al_mirath.model.DelayedConsequence;
import com.example.al_mirath.model.FactionRelations;
import com.example.al_mirath.model.GameEvent;
import com.example.al_mirath.model.GameSnapshot;
import com.example.al_mirath.model.PlayerCharacter;
import com.example.al_mirath.model.WorldEvent;
import com.example.al_mirath.model.WorldState;
import com.example.al_mirath.model.EndingResult;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.HashMap;


public class GameEngine {

    private final PlayerCharacter player;
    private final FactionRelations factions;
    private final WorldState worldState;
    private final List<GameEvent> eventPool;
    private final Random random = new Random();
    private final Map<String, Integer> stageEventsPlayed = new HashMap<>();
    private final String birthIntroMessage;
    private final RecurringCharacterRegistry recurringCharacters;

    private final Map<String, Integer> maxEventsByStage = Map.of(
            "Childhood", 5,
            "Youth", 6,
            "Adulthood", 9,
            "Political Crisis", 6,
            "Legacy", 4
    );

    private final List<String> lifeStages = List.of(
            "Childhood",
            "Youth",
            "Adulthood",
            "Political Crisis",
            "Legacy"
    );

    private final Set<String> playedEventTitles = new HashSet<>();

    private int currentStageIndex = 0;
    private GameEvent currentEvent;
    private EndingResult endingResult;
    private String latestLegacyTitleMessage = "";
    private String latestStatusChangeMessage = "";

    /**
     * Threads of Fate: the currency spent to attempt a rewind. A life starts
     * with one, and more are earned by surviving each life stage.
     */
    private int fateTokens = 1;

    /** State captured immediately before the most recent choice was applied. */
    private GameSnapshot lastSnapshot;

    private int choicesMade = 0;
    private int rewindsUsed = 0;
    private int minigamesWon = 0;
    private int minigamesLost = 0;

    /** World events already shown this life, so the same news never repeats. */
    private final Set<String> shownWorldEvents = new HashSet<>();
    private String latestWorldEventTitle = "";
    private String latestWorldEventMessage = "";

    /**
     * Consequences planted by earlier choices, waiting for the character to
     * reach their trigger age. Keyed by id so a flag set twice cannot schedule
     * the same echo twice.
     */
    private final Map<String, DelayedConsequence> pendingConsequences = new HashMap<>();
    private final Set<String> firedConsequences = new HashSet<>();

    private String latestConsequenceEchoTitle = "";
    private String latestConsequenceEchoMessage = "";

    public GameEngine() {
        CharacterGenerator generator = new CharacterGenerator();
        this.player = generator.generateCharacter();
        this.factions = new FactionRelations();
        this.worldState = new WorldState();

        this.birthIntroMessage = createBirthIntroMessage();

        this.recurringCharacters =
                RecurringCharacterRegistry.createFor(player);

        this.eventPool = EventLibrary.createEventPool();

        this.eventPool.addAll(
                RecurringCharacterEvents.create(
                        recurringCharacters
                )
        );
    }

    /**
     * Restore constructor used when loading a saved game.
     * Rebuilds the engine's internal state from previously saved values
     * instead of generating a brand-new character.
     */
    private GameEngine(
            PlayerCharacter player,
            FactionRelations factions,
            WorldState worldState,
            RecurringCharacterRegistry recurringCharacters,
            int currentStageIndex,
            Map<String, Integer> stageEventsPlayed,
            Set<String> playedEventTitles,
            String currentEventTitle
    ) {
        this.player = player;
        this.factions = factions;
        this.worldState = worldState;

        this.birthIntroMessage = createBirthIntroMessage();

        this.recurringCharacters =
                recurringCharacters == null
                        ? RecurringCharacterRegistry.createFor(player)
                        : recurringCharacters;

        this.eventPool = EventLibrary.createEventPool();

        this.eventPool.addAll(
                RecurringCharacterEvents.create(
                        this.recurringCharacters
                )
        );

        this.currentStageIndex = currentStageIndex;
        this.stageEventsPlayed.putAll(stageEventsPlayed);
        this.playedEventTitles.addAll(playedEventTitles);

        if (currentEventTitle != null) {
            for (GameEvent event : eventPool) {
                if (event.getTitle().equals(currentEventTitle)) {
                    this.currentEvent = event;
                    break;
                }
            }
        }
    }

    /**
     * Serializes the full engine state (player, factions, world flags,
     * progression) to a JSON string so it can be persisted and restored later.
     */
    public String toSaveJson() {
        JSONObject root = new JSONObject();

        JSONObject playerJson = new JSONObject();
        playerJson.put("name", player.getName());
        playerJson.put("era", player.getEra());
        playerJson.put("origin", player.getOrigin());
        playerJson.put("familyCondition", player.getFamilyCondition());
        playerJson.put("trait", player.getTrait());
        playerJson.put("age", player.getAge());
        playerJson.put("currentStatus", player.getCurrentStatus());
        playerJson.put("health", player.getHealth());
        playerJson.put("wealth", player.getWealth());
        playerJson.put("education", player.getEducation());
        playerJson.put("reputation", player.getReputation());
        playerJson.put("politicalPower", player.getPoliticalPower());
        playerJson.put("morality", player.getMorality());
        playerJson.put("familyLoyalty", player.getFamilyLoyalty());
        playerJson.put("stress", player.getStress());
        playerJson.put("legacyTitles", new JSONArray(player.getLegacyTitles()));
        root.put("player", playerJson);

        JSONObject factionsJson = new JSONObject();
        factionsJson.put("court", factions.getCourt());
        factionsJson.put("nobles", factions.getNobles());
        factionsJson.put("military", factions.getMilitary());
        factionsJson.put("scholars", factions.getScholars());
        factionsJson.put("merchants", factions.getMerchants());
        factionsJson.put("commonPeople", factions.getCommonPeople());
        factionsJson.put("familyCouncil", factions.getFamilyCouncil());
        factionsJson.put("shadowNetwork", factions.getShadowNetwork());
        root.put("factions", factionsJson);

        root.put("worldFlags", new JSONArray(worldState.getFlags()));
        root.put("currentStageIndex", currentStageIndex);
        root.put("stageEventsPlayed", new JSONObject(stageEventsPlayed));
        root.put("playedEventTitles", new JSONArray(playedEventTitles));
        root.put("currentEventTitle", currentEvent == null ? JSONObject.NULL : currentEvent.getTitle());

        root.put("fateTokens", fateTokens);
        root.put("stagesRewarded", new JSONArray(stagesRewarded));
        root.put("choicesMade", choicesMade);
        root.put("rewindsUsed", rewindsUsed);
        root.put("minigamesWon", minigamesWon);
        root.put("minigamesLost", minigamesLost);
        root.put("shownWorldEvents", new JSONArray(shownWorldEvents));
        root.put("firedConsequences", new JSONArray(firedConsequences));

        // Only ids and trigger ages are stored; the full text is rebuilt from
        // ConsequenceLibrary on load, so edits to that content reach saves in
        // progress instead of being frozen at the moment of scheduling.
        JSONObject pendingJson = new JSONObject();
        for (DelayedConsequence consequence : pendingConsequences.values()) {
            pendingJson.put(consequence.id(), consequence.triggerAge());
        }
        root.put("pendingConsequences", pendingJson);

        root.put(
                "recurringCharacters",
                recurringCharacters.toJson()
        );

        return root.toString();
    }

    /**
     * Rebuilds a GameEngine from a JSON string previously produced by
     * {@link #toSaveJson()}.
     */
    public static GameEngine fromSaveJson(String json) {
        JSONObject root = new JSONObject(json);

        JSONObject playerJson = root.getJSONObject("player");

        PlayerCharacter player = new PlayerCharacter(
                playerJson.getString("name"),
                playerJson.getString("era"),
                playerJson.getString("origin"),
                playerJson.getString("familyCondition"),
                playerJson.getString("trait"),
                playerJson.getInt("age"),
                playerJson.getInt("health"),
                playerJson.getInt("wealth"),
                playerJson.getInt("education"),
                playerJson.getInt("reputation"),
                playerJson.getInt("politicalPower"),
                playerJson.getInt("morality"),
                playerJson.getInt("familyLoyalty"),
                playerJson.getInt("stress")
        );

        player.setCurrentStatus(playerJson.getString("currentStatus"));

        RecurringCharacterRegistry recurringCharacters;

        if (root.has("recurringCharacters")) {
            recurringCharacters =
                    RecurringCharacterRegistry.fromJson(
                            root.getJSONObject(
                                    "recurringCharacters"
                            )
                    );
        } else {
            recurringCharacters =
                    RecurringCharacterRegistry.createFor(player);
        }

        JSONArray legacyTitlesJson = playerJson.getJSONArray("legacyTitles");
        for (int i = 0; i < legacyTitlesJson.length(); i++) {
            player.addLegacyTitle(legacyTitlesJson.getString(i));
        }

        JSONObject factionsJson = root.getJSONObject("factions");
        FactionRelations factions = new FactionRelations(
                factionsJson.getInt("court"),
                factionsJson.getInt("nobles"),
                factionsJson.getInt("military"),
                factionsJson.getInt("scholars"),
                factionsJson.getInt("merchants"),
                factionsJson.getInt("commonPeople"),
                factionsJson.getInt("familyCouncil"),
                factionsJson.getInt("shadowNetwork")
        );

        WorldState worldState = new WorldState();
        JSONArray worldFlagsJson = root.getJSONArray("worldFlags");
        List<String> worldFlags = new ArrayList<>();
        for (int i = 0; i < worldFlagsJson.length(); i++) {
            worldFlags.add(worldFlagsJson.getString(i));
        }
        worldState.addFlags(worldFlags);

        Map<String, Integer> stageEventsPlayed = new HashMap<>();
        JSONObject stageEventsJson = root.getJSONObject("stageEventsPlayed");
        for (String key : stageEventsJson.keySet()) {
            stageEventsPlayed.put(key, stageEventsJson.getInt(key));
        }

        Set<String> playedEventTitles = new HashSet<>();
        JSONArray playedEventTitlesJson = root.getJSONArray("playedEventTitles");
        for (int i = 0; i < playedEventTitlesJson.length(); i++) {
            playedEventTitles.add(playedEventTitlesJson.getString(i));
        }

        String currentEventTitle = root.isNull("currentEventTitle")
                ? null
                : root.getString("currentEventTitle");

        GameEngine engine = new GameEngine(
                player,
                factions,
                worldState,
                recurringCharacters,
                root.getInt("currentStageIndex"),
                stageEventsPlayed,
                playedEventTitles,
                currentEventTitle
        );

        engine.restoreProgression(root);

        return engine;
    }

    /**
     * Reads the Threads of Fate economy and run counters back out of a save.
     * Every field is optional so saves written before these existed still load.
     */
    private void restoreProgression(JSONObject root) {
        this.fateTokens = root.optInt("fateTokens", 1);
        this.choicesMade = root.optInt("choicesMade", 0);
        this.rewindsUsed = root.optInt("rewindsUsed", 0);
        this.minigamesWon = root.optInt("minigamesWon", 0);
        this.minigamesLost = root.optInt("minigamesLost", 0);

        JSONArray rewarded = root.optJSONArray("stagesRewarded");

        if (rewarded != null) {
            for (int i = 0; i < rewarded.length(); i++) {
                this.stagesRewarded.add(rewarded.getString(i));
            }
        }

        JSONArray worldEvents = root.optJSONArray("shownWorldEvents");

        if (worldEvents != null) {
            for (int i = 0; i < worldEvents.length(); i++) {
                this.shownWorldEvents.add(worldEvents.getString(i));
            }
        }

        JSONArray fired = root.optJSONArray("firedConsequences");

        if (fired != null) {
            for (int i = 0; i < fired.length(); i++) {
                this.firedConsequences.add(fired.getString(i));
            }
        }

        JSONObject pending = root.optJSONObject("pendingConsequences");

        if (pending != null) {
            for (String id : pending.keySet()) {
                DelayedConsequence restored = ConsequenceLibrary.byId(id, pending.getInt(id));

                if (restored != null) {
                    this.pendingConsequences.put(id, restored);
                }
            }
        }
    }

    public String getBirthIntroMessage() {
        return birthIntroMessage;
    }

    public RecurringCharacterRegistry getRecurringCharacters() {
        return recurringCharacters;
    }

    public String getRecurringCharacterSummary() {
        return recurringCharacters.relationshipSummary();
    }

    public String consumeLatestStatusChangeMessage() {
        String message = latestStatusChangeMessage;
        latestStatusChangeMessage = "";
        return message;
    }

    /** Empty when there is no delayed echo pending; check before showing the popup. */
    public String getLatestConsequenceEchoTitle() {
        return latestConsequenceEchoTitle;
    }

    public String consumeLatestConsequenceEchoMessage() {
        String message = latestConsequenceEchoMessage;
        latestConsequenceEchoTitle = "";
        latestConsequenceEchoMessage = "";
        return message;
    }

    /** Number of echoes still waiting to come due; surfaced for testing. */
    public int getPendingConsequenceCount() {
        return pendingConsequences.size();
    }

    /**
     * Plants whatever long-term echoes a newly set flag carries. Ids already
     * scheduled or already fired are skipped, so re-setting a flag cannot
     * stack duplicates.
     */
    private void scheduleConsequencesFor(String flag) {
        for (DelayedConsequence consequence : ConsequenceLibrary.consequencesFor(flag, player.getAge())) {
            if (firedConsequences.contains(consequence.id())) {
                continue;
            }

            pendingConsequences.putIfAbsent(consequence.id(), consequence);
        }
    }

    /**
     * Fires at most one due echo per choice, so several coming due together
     * are spread across turns rather than buried in a single popup.
     */
    private void fireDueConsequences() {
        if (!latestConsequenceEchoMessage.isBlank()) {
            return;
        }

        DelayedConsequence due = null;

        for (DelayedConsequence consequence : pendingConsequences.values()) {
            if (player.getAge() < consequence.triggerAge()) {
                continue;
            }

            // A later choice may have defused this entirely.
            if (consequence.hasCancelFlag() && worldState.hasFlag(consequence.cancelledByFlag())) {
                pendingConsequences.remove(consequence.id());
                firedConsequences.add(consequence.id());
                return;
            }

            if (!worldState.hasAllFlags(consequence.requiredFlags())) {
                continue;
            }

            due = consequence;
            break;
        }

        if (due == null) {
            return;
        }

        pendingConsequences.remove(due.id());
        firedConsequences.add(due.id());

        StringBuilder effectLines = new StringBuilder();

        for (Map.Entry<String, Integer> effect : due.statEffects().entrySet()) {
            player.applyChange(effect.getKey(), effect.getValue());
            appendEffectLine(effectLines, statDisplayName(effect.getKey()), effect.getValue());
        }

        for (Map.Entry<String, Integer> effect : due.factionEffects().entrySet()) {
            factions.applyChange(effect.getKey(), effect.getValue());
            appendEffectLine(effectLines, factionDisplayName(effect.getKey()), effect.getValue());
        }

        for (String flag : due.flagsToAdd()) {
            worldState.addFlag(flag);
        }

        latestConsequenceEchoTitle = due.title();
        latestConsequenceEchoMessage = effectLines.isEmpty()
                ? due.description()
                : due.description() + "\n\nWhat it costs you now:\n" + effectLines;
    }

    /** Empty when there is no world event pending; check before showing the popup. */
    public String getLatestWorldEventTitle() {
        return latestWorldEventTitle;
    }

    public String consumeLatestWorldEventMessage() {
        String message = latestWorldEventMessage;
        latestWorldEventTitle = "";
        latestWorldEventMessage = "";
        return message;
    }

    /**
     * A modest chance each choice to surface unrelated news from the wider
     * world — a death, a rebellion, a plague — so the setting keeps moving
     * even when the player's own path stays quiet. Never repeats within a life.
     */
    private void rollForWorldEvent() {
        if (!latestWorldEventMessage.isBlank()) {
            return;
        }

        if (random.nextInt(100) >= 22) {
            return;
        }

        List<WorldEvent> candidates = new ArrayList<>();

        for (WorldEvent event : WorldEventLibrary.all()) {
            if (!shownWorldEvents.contains(event.title()) && event.isEraAllowed(player.getEra())) {
                candidates.add(event);
            }
        }

        if (candidates.isEmpty()) {
            return;
        }

        WorldEvent chosen = candidates.get(random.nextInt(candidates.size()));
        shownWorldEvents.add(chosen.title());

        latestWorldEventTitle = chosen.title();
        latestWorldEventMessage = applyWorldEventEffects(chosen);
    }

    /**
     * Applies a world event's stat and faction effects to the live character,
     * and returns the description with an effect summary appended so the
     * player can see that the news actually touched their life.
     */
    private String applyWorldEventEffects(WorldEvent event) {
        if (!event.hasEffects()) {
            return event.description();
        }

        StringBuilder effectLines = new StringBuilder();

        for (Map.Entry<String, Integer> effect : event.statEffects().entrySet()) {
            player.applyChange(effect.getKey(), effect.getValue());
            appendEffectLine(effectLines, statDisplayName(effect.getKey()), effect.getValue());
        }

        for (Map.Entry<String, Integer> effect : event.factionEffects().entrySet()) {
            factions.applyChange(effect.getKey(), effect.getValue());
            appendEffectLine(effectLines, factionDisplayName(effect.getKey()), effect.getValue());
        }

        if (effectLines.isEmpty()) {
            return event.description();
        }

        return event.description() + "\n\nHow it touches your life:\n" + effectLines;
    }

    private void appendEffectLine(StringBuilder report, String displayName, int delta) {
        if (delta == 0) {
            return;
        }

        if (!report.isEmpty()) {
            report.append("\n");
        }

        report.append(delta > 0 ? "+" : "").append(delta).append(" ").append(displayName);
    }

    private String statDisplayName(String stat) {
        return switch (stat) {
            case "health" -> "Health";
            case "wealth" -> "Wealth";
            case "education" -> "Education";
            case "reputation" -> "Reputation";
            case "politicalPower" -> "Political Power";
            case "morality" -> "Morality";
            case "familyLoyalty" -> "Family Loyalty";
            case "stress" -> "Stress";
            default -> stat;
        };
    }

    private String factionDisplayName(String faction) {
        return switch (faction) {
            case "court" -> "Court";
            case "nobles" -> "Nobles";
            case "military" -> "Military";
            case "scholars" -> "Scholars";
            case "merchants" -> "Merchants";
            case "commonPeople" -> "Common People";
            case "familyCouncil" -> "Family Council";
            case "shadowNetwork" -> "Shadow Network";
            default -> faction;
        };
    }

    private String createBirthIntroMessage() {
        return "A new life begins...\n\n"
                + "You are born during the " + player.getEra() + ".\n\n"
                + "Origin: " + player.getOrigin() + "\n"
                + "Family Condition: " + player.getFamilyCondition() + "\n"
                + "Trait: " + player.getTrait() + "\n\n"
                + getFamilyBirthDescription()
                + "\n\nYour story begins at age " + player.getAge()
                + ". The world does not yet know your name, but your choices will decide whether your life becomes forgotten, honored, feared, or remembered.";
    }

    private String getFamilyBirthDescription() {
        return switch (player.getFamilyCondition()) {
            case "Stable Household" ->
                    "You are born into a household with enough order to give you a fair beginning, though no future is guaranteed.";

            case "Debt-Burdened Family" ->
                    "Your family carries debt before you are old enough to understand money. Survival will not be easy.";

            case "Disgraced Bloodline" ->
                    "Your family name carries shame. Some doors are already closed before you even begin your life.";

            case "Recently Orphaned" ->
                    "Loss reaches your life early. Without normal protection, you must grow stronger than other children.";

            case "Family Divided by Rivalry" ->
                    "Your household is divided by suspicion and rivalry. Even family loyalty may become a test.";

            case "Secret Noble Blood" ->
                    "There are whispers that your bloodline is greater than it appears. If true, this secret could become power or danger.";

            case "Political Enemy of the Court" ->
                    "Your family is already disliked by the court. Power watches your household with cold eyes.";

            case "Exiled Branch" ->
                    "You are born into a branch pushed away from its former place. Return, revenge, or survival may define your path.";

            case "Favored by Local Scholars" ->
                    "Local scholars respect your household. Knowledge may open doors that wealth cannot.";

            case "Watched by Palace Spies" ->
                    "From your earliest years, hidden eyes follow your family. One wrong step may be remembered.";

            default ->
                    "Your family background shapes your first steps, but your choices will shape the rest.";
        };
    }

    public PlayerCharacter getPlayer() {
        return player;
    }

    public FactionRelations getFactions() {
        return factions;
    }

    public GameEvent getCurrentEvent() {
        if (!player.isAlive()) {
            currentEvent = null;

            if (endingResult == null) {
                endingResult = calculateEnding();
            }

            return null;
        }

        if (currentEvent == null) {
            currentEvent = selectEventForCurrentStage();
        }

        if (currentEvent == null && endingResult == null) {
            endingResult = calculateEnding();
        }

        return currentEvent;
    }

    private GameEvent selectEventForCurrentStage() {
        while (currentStageIndex < lifeStages.size()) {
            String stage = lifeStages.get(currentStageIndex);

            int playedInStage = stageEventsPlayed.getOrDefault(stage, 0);
            int maxForStage = maxEventsByStage.getOrDefault(stage, 1);

            if (playedInStage >= maxForStage) {
                awardStageCompletionToken(stage);
                currentStageIndex++;
                continue;
            }

            GameEvent consequenceEvent = findEventForStage(stage, true);

            if (consequenceEvent != null) {
                return consequenceEvent;
            }

            GameEvent normalEvent = findEventForStage(stage, false);

            if (normalEvent != null) {
                return normalEvent;
            }

            currentStageIndex++;
        }

        return null;
    }

    private GameEvent findEventForStage(String stage, boolean consequenceOnly) {
        List<GameEvent> eligibleEvents = new ArrayList<>();

        for (GameEvent event : eventPool) {
            if (playedEventTitles.contains(event.getTitle())) {
                continue;
            }

            if (!event.getLifeStage().equals(stage)) {
                continue;
            }

            if (consequenceOnly && !event.hasRequiredFlags()) {
                continue;
            }

            if (!consequenceOnly && event.hasRequiredFlags()) {
                continue;
            }

            if (event.isEligibleFor(player, worldState)) {
                eligibleEvents.add(event);
            }
        }

        if (eligibleEvents.isEmpty()) {
            return null;
        }

        return eligibleEvents.get(random.nextInt(eligibleEvents.size()));
    }

    public String applyChoice(Choice choice) {
        return applyChoice(choice, null);
    }

    /**
     * Applies a choice, optionally with the outcome already decided.
     *
     * @param forcedOutcome when non-null, overrides the internal roll. This is
     *                      how a Trial of Skill works: the player earns the
     *                      success or failure by playing a challenge, instead
     *                      of the engine rolling against a stat behind the
     *                      scenes.
     */
    public String applyChoice(Choice choice, Boolean forcedOutcome) {
        if (!canChoose(choice)) {
            return "This choice is unavailable.\n\n" + getLockedReason(choice);
        }

        // Captured before anything mutates so a won rewind lands exactly here.
        lastSnapshot = captureSnapshot(choice);
        choicesMade++;

        boolean success = forcedOutcome != null
                ? forcedOutcome
                : resolveChoiceSuccess(choice);

        Map<String, Integer> statEffects;
        Map<String, Integer> factionEffects;
        String resultText;
        List<String> flagsToAdd;

        if (success) {
            statEffects = choice.getSuccessStatEffects();
            factionEffects = choice.getSuccessFactionEffects();
            resultText = choice.getSuccessText();
            flagsToAdd = choice.getSuccessFlags();
        } else {
            statEffects = choice.getFailureStatEffects();
            factionEffects = choice.getFailureFactionEffects();
            resultText = choice.getFailureText();
            flagsToAdd = choice.getFailureFlags();
        }

        for (Map.Entry<String, Integer> effect : statEffects.entrySet()) {
            player.applyChange(effect.getKey(), effect.getValue());
        }

        for (Map.Entry<String, Integer> effect : factionEffects.entrySet()) {
            factions.applyChange(effect.getKey(), effect.getValue());
        }

        for (String flag : flagsToAdd) {
            worldState.addFlag(flag);
            scheduleConsequencesFor(flag);

            recurringCharacters.applyStoryFlag(
                    flag,
                    player.getAge()
            );
        }

        int yearsPassed = advanceAgeAfterEvent();

        recurringCharacters.ageEveryone(
                yearsPassed
        );
        recoverStressOverTime(yearsPassed);
        updateCurrentStatus();
        checkMortalityAfterChoice(choice, success);

        // Checked after ageing, so an echo comes due the moment the years
        // catch up with the choice that planted it.
        fireDueConsequences();

        latestLegacyTitleMessage = checkForNewLegacyTitles();
        rollForWorldEvent();

        if (currentEvent != null) {
            playedEventTitles.add(currentEvent.getTitle());
        }

        if (currentEvent != null) {
            String stage = currentEvent.getLifeStage();
            int playedInStage = stageEventsPlayed.getOrDefault(stage, 0);
            stageEventsPlayed.put(stage, playedInStage + 1);
        }

        currentEvent = null;

        String finalResult;

        if (choice.requiresStatCheck()) {
            finalResult = buildCheckedResultText(success, resultText);
        } else {
            finalResult = resultText;
        }

        return finalResult;
    }

    /** Stages that have already paid out their completion thread. */
    private final Set<String> stagesRewarded = new HashSet<>();

    private String latestFateTokenMessage = "";

    /**
     * Surviving a life stage earns one Thread of Fate, capped so the player
     * cannot bank enough to trivialise a whole run.
     */
    private void awardStageCompletionToken(String stage) {
        if (!stagesRewarded.add(stage)) {
            return;
        }

        if (fateTokens >= 3) {
            return;
        }

        fateTokens++;

        latestFateTokenMessage =
                "You have survived " + stage + ".\n\n"
                        + "A Thread of Fate is added to your hand. "
                        + "Threads let you challenge destiny to undo a decision you regret.\n\n"
                        + "Threads held: " + fateTokens;
    }

    public String consumeLatestFateTokenMessage() {
        String message = latestFateTokenMessage;
        latestFateTokenMessage = "";
        return message;
    }

    private static final List<String> STAT_KEYS = List.of(
            "health", "wealth", "education", "reputation",
            "politicalPower", "morality", "familyLoyalty", "stress"
    );

    private static final List<String> FACTION_KEYS = List.of(
            "court", "nobles", "military", "scholars",
            "merchants", "commonPeople", "familyCouncil", "shadowNetwork"
    );

    /**
     * Records everything a choice is about to touch.
     */
    private GameSnapshot captureSnapshot(Choice choice) {
        Map<String, Integer> stats = new HashMap<>();
        for (String key : STAT_KEYS) {
            stats.put(key, player.getStatValue(key));
        }

        Map<String, Integer> factionValues = new HashMap<>();
        for (String key : FACTION_KEYS) {
            factionValues.put(key, factions.getValue(key));
        }

        return new GameSnapshot(
                player.getName(),
                player.getAge(),
                player.isAlive(),
                player.getDeathReason(),
                player.getCurrentStatus(),
                player.getLegacyTitles(),
                stats,
                factionValues,
                worldState.getFlags(),
                currentStageIndex,
                stageEventsPlayed,
                playedEventTitles,
                currentEvent == null ? null : currentEvent.getTitle(),
                currentEvent == null ? "" : currentEvent.getTitle(),
                currentEvent == null ? "" : currentEvent.getLifeStage(),
                choice == null ? "" : choice.getText(),
                recurringCharacters.toJson().toString()
        );
    }

    /** True when there is a decision available to undo and a thread to spend. */
    public boolean canRewind() {
        return lastSnapshot != null && fateTokens > 0;
    }

    public int getFateTokens() {
        return fateTokens;
    }

    public GameSnapshot getLastSnapshot() {
        return lastSnapshot;
    }

    /** Read-only view of the story flags this life has accumulated. */
    public Set<String> getWorldFlags() {
        return Set.copyOf(worldState.getFlags());
    }

    public int getCurrentStageIndex() {
        return currentStageIndex;
    }

    /** The life stage currently in play, e.g. "Youth" or "Legacy". */
    public String getCurrentLifeStage() {
        int index = Math.max(0, Math.min(currentStageIndex, lifeStages.size() - 1));
        return lifeStages.get(index);
    }

    /** Returns a thread spent on a trial the player backed out of. */
    public void refundFateToken() {
        fateTokens++;
    }

    /**
     * Spends a thread to open a rewind attempt. The token is consumed whether
     * or not the challenge is then won, so attempting one is a real gamble.
     *
     * @return false when there is nothing to undo or no thread left
     */
    public boolean spendFateToken() {
        if (!canRewind()) {
            return false;
        }

        fateTokens--;
        return true;
    }

    /**
     * Restores the run to the moment before the last choice was applied.
     * Called only after a challenge has been won.
     *
     * @return false when there is no snapshot to return to
     */
    public boolean rewindToLastSnapshot() {
        if (lastSnapshot == null) {
            return false;
        }

        GameSnapshot snapshot = lastSnapshot;

        for (Map.Entry<String, Integer> entry : snapshot.getStats().entrySet()) {
            player.setStatValue(entry.getKey(), entry.getValue());
        }

        for (Map.Entry<String, Integer> entry : snapshot.getFactions().entrySet()) {
            factions.setValue(entry.getKey(), entry.getValue());
        }

        player.setAge(snapshot.getAge());
        player.setCurrentStatus(snapshot.getCurrentStatus());
        player.replaceLegacyTitles(snapshot.getLegacyTitles());

        if (snapshot.isAlive()) {
            player.restoreLife();
        }

        worldState.replaceFlags(snapshot.getWorldFlags());

        String recurringCharactersJson =
                snapshot.getRecurringCharactersJson();

        if (recurringCharactersJson != null
                && !recurringCharactersJson.isBlank()) {

            RecurringCharacterRegistry restoredCharacters =
                    RecurringCharacterRegistry.fromJson(
                            new JSONObject(
                                    recurringCharactersJson
                            )
                    );

            recurringCharacters.replaceWith(
                    restoredCharacters
            );
        }

        currentStageIndex = snapshot.getCurrentStageIndex();

        stageEventsPlayed.clear();
        stageEventsPlayed.putAll(snapshot.getStageEventsPlayed());

        playedEventTitles.clear();
        playedEventTitles.addAll(snapshot.getPlayedEventTitles());

        // Put the player back in front of the same event, not a fresh roll.
        currentEvent = null;
        String title = snapshot.getCurrentEventTitle();

        if (title != null) {
            for (GameEvent event : eventPool) {
                if (event.getTitle().equals(title)) {
                    currentEvent = event;
                    break;
                }
            }
        }

        // A rewound life has no ending yet, and pending messages belonged to
        // the choice that no longer happened.
        endingResult = null;
        latestLegacyTitleMessage = "";
        latestStatusChangeMessage = "";
        latestWorldEventTitle = "";
        latestWorldEventMessage = "";
        latestConsequenceEchoTitle = "";
        latestConsequenceEchoMessage = "";

        lastSnapshot = null;
        rewindsUsed++;

        return true;
    }

    /**
     * True when the life avoided every corrupt shortcut: no bribery, no sold
     * secrets, and no debt to the shadow network.
     */
    public boolean playedCleanRun() {
        return !worldState.hasFlag("used_bribery")
                && !worldState.hasFlag("used_shadow_contacts")
                && !worldState.hasFlag("sold_palace_secret")
                && !worldState.hasFlag("owed_shadow_debt")
                && !worldState.hasFlag("served_shadow_network")
                && !worldState.hasFlag("framed_innocent");
    }

    /**
     * Scores a finished life for the records screen and personal bests.
     *
     * <p>Weighted so that a long life with earned titles and broad standing
     * beats one that simply maxed a single stat, and so stress counts against
     * the total.
     */
    public int calculateScore() {
        int statTotal =
                player.getHealth()
                        + player.getWealth()
                        + player.getEducation()
                        + player.getReputation()
                        + player.getPoliticalPower()
                        + player.getMorality()
                        + player.getFamilyLoyalty();

        int factionTotal = 0;
        for (String key : FACTION_KEYS) {
            factionTotal += factions.getValue(key);
        }

        int score = statTotal * 3;
        score += factionTotal;
        score += player.getLegacyTitles().size() * 250;
        score += player.getAge() * 8;
        score -= player.getStress() * 2;

        // Earning the ending under your own steam is worth more than rewinding into it.
        score -= rewindsUsed * 120;

        if (playedCleanRun()) {
            score += 400;
        }

        return Math.max(0, score);
    }

    public void recordMinigameOutcome(boolean won) {
        if (won) {
            minigamesWon++;
        } else {
            minigamesLost++;
        }
    }

    /** Clears the undo point, e.g. once a consequence has been accepted. */
    public void clearSnapshot() {
        lastSnapshot = null;
    }

    public int getChoicesMade() {
        return choicesMade;
    }

    public int getRewindsUsed() {
        return rewindsUsed;
    }

    public int getMinigamesWon() {
        return minigamesWon;
    }

    public int getMinigamesLost() {
        return minigamesLost;
    }

    private boolean resolveChoiceSuccess(Choice choice) {
        if (!choice.requiresStatCheck()) {
            return true;
        }

        int statValue = player.getStatValue(choice.getCheckStat());
        int difficulty = choice.getDifficulty();

        int successChance = 50 + (statValue - difficulty);
        successChance = Math.max(10, Math.min(90, successChance));

        int roll = random.nextInt(100) + 1;

        return roll <= successChance;
    }

    private String buildCheckedResultText(boolean success, String resultText) {
        String outcome = success ? "Outcome: Success" : "Outcome: Failure";
        return outcome + "\n\n" + resultText;
    }

    private int advanceAgeAfterEvent() {
        if (currentEvent == null) {
            return 0;
        }

        String stage = currentEvent.getLifeStage();
        int yearsPassed;

        switch (stage) {
            case "Childhood" -> yearsPassed = random.nextInt(2) + 1; // 1–2 years
            case "Youth" -> yearsPassed = random.nextInt(2) + 1; // 1–2 years
            case "Adulthood" -> yearsPassed = random.nextInt(3) + 1; // 1–3 years
            case "Political Crisis" -> yearsPassed = random.nextInt(3) + 1; // 1–3 years
            case "Legacy" -> yearsPassed = random.nextInt(5) + 2; // 2–6 years
            default -> yearsPassed = 1;
        }

        player.increaseAge(yearsPassed);

        return yearsPassed;
    }

    /**
     * Time between events brings some natural relief from pressure, on top of
     * whatever the choice itself did to stress. Combined with the dampening in
     * {@link PlayerCharacter#applyChange}, this keeps stress from rushing to
     * 100 within the first few choices of a life.
     */
    private void recoverStressOverTime(int yearsPassed) {
        if (yearsPassed <= 0 || player.getStress() <= 0) {
            return;
        }

        int recovery = Math.min(6, yearsPassed * 3);
        player.applyChange("stress", -recovery);
    }

    private void checkMortalityAfterChoice(Choice choice, boolean success) {
        if (!player.isAlive()) {
            return;
        }

        int risk = 0;
        String reason = "";

        // Very low health is dangerous at any age
        if (player.getHealth() <= 10) {
            risk += 45;
            reason = "Your body finally failed after years of hardship.";
        } else if (player.getHealth() <= 20) {
            risk += 25;
            reason = "Your weakened body could not fully recover from the burdens of life.";
        } else if (player.getHealth() <= 35) {
            risk += 10;
            reason = "Poor health made every crisis more dangerous.";
        }

        // Stress makes health risk worse
        if (player.getStress() >= 90) {
            risk += 25;
            reason = "The pressure of your life became too heavy to survive.";
        } else if (player.getStress() >= 75) {
            risk += 12;
            reason = "Years of pressure weakened your chance of survival.";
        }

        // Age risk
        if (player.getAge() >= 90) {
            risk += 55;
            reason = "Old age closed the final chapter of your life.";
        } else if (player.getAge() >= 80) {
            risk += 30;
            reason = "Age made every burden harder to carry.";
        } else if (player.getAge() >= 70) {
            risk += 15;
            reason = "You had entered the dangerous final years of life.";
        } else if (player.getAge() >= 60) {
            risk += 6;
            reason = "Age slowly began to weaken your body.";
        }

        // Dangerous political consequences
        if (worldState.hasFlag("court_suspicion") && factions.getCourt() <= 25) {
            risk += 20;
            reason = "Court suspicion surrounded your final days.";
        }

        if (worldState.hasFlag("collector_reported_you") && factions.getCourt() <= 30) {
            risk += 18;
            reason = "The report against you destroyed your protection.";
        }

        if (worldState.hasFlag("owed_shadow_debt") && factions.getShadowNetwork() >= 70) {
            risk += 18;
            reason = "The shadow network protected you for too long to let you walk away freely.";
        }

        if (worldState.hasFlag("framed_innocent") && factions.getCourt() <= 30) {
            risk += 15;
            reason = "The lie you built began to collapse around you.";
        }

        // Failed stat-check choices are more dangerous
        if (choice.requiresStatCheck() && !success) {
            risk += 10;

            if (choice.getCheckStat().equals("health")) {
                risk += 15;
                reason = "A failed physical challenge left lasting damage.";
            }

            if (choice.getCheckStat().equals("politicalPower")) {
                risk += 10;
                reason = "A failed power move exposed you to dangerous enemies.";
            }
        }

        // Young death should be possible, but only under heavy danger
        if (player.getAge() < 25 && player.getHealth() > 35 && player.getStress() < 80) {
            risk = Math.min(risk, 8);
        }

        // Keep risk controlled
        risk = Math.max(0, Math.min(75, risk));

        int roll = random.nextInt(100) + 1;

        if (roll <= risk) {
            if (reason.isEmpty()) {
                reason = "Your life ended before your ambitions could fully unfold.";
            }

            player.markDead(reason);
        }
    }

    private String checkForNewLegacyTitles() {
        StringBuilder message = new StringBuilder();

        if (worldState.hasFlag("protected_commoners")
                && player.getReputation() >= 55
                && factions.getCommonPeople() >= 65) {
            addTitleMessage(message, "Hero of the People");
        }

        if (worldState.hasFlag("protected_commoners")
                && factions.getCommonPeople() >= 70
                && player.getWealth() <= 20) {
            addTitleMessage(message, "Voice of the Poor");
        }

        if (player.getEducation() >= 80
                && factions.getScholars() >= 70
                && player.getReputation() >= 45) {
            addTitleMessage(message, "Light of the Madrasa");
        }

        if (player.getEducation() >= 75
                && player.getMorality() >= 75
                && factions.getScholars() >= 65) {
            addTitleMessage(message, "The Wise Judge");
        }

        if (worldState.hasFlag("angered_scholars")
                && player.getEducation() >= 65
                && factions.getScholars() <= 20) {
            addTitleMessage(message, "Heretic in the Court");
        }

        if (player.getPoliticalPower() >= 65
                && factions.getMilitary() >= 70
                && player.getReputation() >= 50) {
            addTitleMessage(message, "Sword of the Realm");
        }

        if (player.getHealth() >= 75
                && factions.getMilitary() >= 70
                && player.getReputation() >= 55) {
            addTitleMessage(message, "Frontier Hero");
        }

        if (player.getMorality() <= 20
                && factions.getMilitary() >= 75
                && player.getPoliticalPower() >= 55) {
            addTitleMessage(message, "Blood General");
        }

        if (player.getWealth() >= 75
                && factions.getMerchants() >= 70) {
            addTitleMessage(message, "Golden Hand");
        }

        if (player.getWealth() >= 65
                && factions.getMerchants() >= 80) {
            addTitleMessage(message, "Master of Caravans");
        }

        if (player.getWealth() >= 80
                && player.getMorality() <= 25) {
            addTitleMessage(message, "Coin-Bound Soul");
        }

        if (worldState.hasFlag("declared_loyalty")
                && player.getPoliticalPower() >= 60
                && factions.getCourt() >= 70) {
            addTitleMessage(message, "Dynasty Loyalist");
        }

        if (factions.getCourt() >= 70
                && player.getStress() >= 80
                && player.getPoliticalPower() >= 50) {
            addTitleMessage(message, "Court Survivor");
        }

        if (player.getPoliticalPower() >= 80
                && player.getReputation() >= 60) {
            addTitleMessage(message, "Rising Power");
        }

        if (factions.getCourt() <= 15
                && player.getPoliticalPower() >= 60) {
            addTitleMessage(message, "Enemy of the Court");
        }

        if ((worldState.hasFlag("used_shadow_contacts")
                || worldState.hasFlag("sold_palace_secret")
                || worldState.hasFlag("learned_palace_secrets"))
                && player.getPoliticalPower() >= 55
                && factions.getShadowNetwork() >= 65) {
            addTitleMessage(message, "Knife in the Dark");
        }

        if (factions.getShadowNetwork() >= 80
                && player.getReputation() <= 35) {
            addTitleMessage(message, "Whisper Lord");
        }

        if ((worldState.hasFlag("betrayed_comrade") || worldState.hasFlag("sold_palace_secret"))
                && player.getMorality() <= 25
                && player.getFamilyLoyalty() <= 40) {
            addTitleMessage(message, "The Betrayer");
        }

        if (player.getFamilyLoyalty() >= 85
                && player.getMorality() >= 60) {
            addTitleMessage(message, "Bloodline Protector");
        }

        if (player.getFamilyCondition().equals("Recently Orphaned")
                && player.getReputation() >= 60
                && player.getStress() >= 70) {
            addTitleMessage(message, "Orphan of Iron");
        }

        if (player.getFamilyCondition().equals("Disgraced Bloodline")
                && player.getReputation() >= 65
                && player.getPoliticalPower() >= 45) {
            addTitleMessage(message, "Restorer of Honor");
        }

        if (player.getFamilyCondition().equals("Exiled Branch")
                && player.getPoliticalPower() >= 65
                && factions.getCourt() >= 50) {
            addTitleMessage(message, "Returned from Exile");
        }

        if (player.getStress() >= 90
                && player.getHealth() >= 45
                && player.getReputation() >= 45) {
            addTitleMessage(message, "Burdened Survivor");
        }

        if (player.getHealth() <= 20
                && player.getReputation() >= 60) {
            addTitleMessage(message, "Fading Legend");
        }

        return message.toString();
    }

    private void addTitleMessage(StringBuilder message, String title) {
        boolean added = player.addLegacyTitle(title);

        if (added) {
            if (!message.isEmpty()) {
                message.append("\n");
            }

            message.append("Legacy Title Gained: ").append(title);
        }
    }

    private void updateCurrentStatus() {
        String oldStatus = player.getCurrentStatus();
        String newStatus;

        // Major story-arc statuses first
        if (worldState.hasFlag("took_throne")) {
            newStatus = "Ruler";

        } else if (worldState.hasFlag("became_regent")) {
            newStatus = "Royal Regent";

        } else if (worldState.hasFlag("failed_claim")) {
            newStatus = "Disgraced Claimant";

        } else if (worldState.hasFlag("family_name_restored")) {
            newStatus = "Restorer of Honor";

        } else if (worldState.hasFlag("new_name_through_power")) {
            newStatus = "Power-Made Noble";

        } else if (worldState.hasFlag("old_name_abandoned")) {
            newStatus = "Self-Made Survivor";

        } else if (worldState.hasFlag("family_restoration_failed")) {
            newStatus = "Disgraced Survivor";

        } else if (worldState.hasFlag("exiled_branch_restored")) {
            newStatus = "Returned from Exile";

        } else if (worldState.hasFlag("minor_return_granted")) {
            newStatus = "Restored Minor Noble";

        } else if (worldState.hasFlag("outside_power_base")) {
            newStatus = "Power Outside Court";

        } else if (worldState.hasFlag("restoration_refused")) {
            newStatus = "Unrecognized Returnee";

        } else if (worldState.hasFlag("popular_uprising_leader")) {
            newStatus = "People's Leader";

        } else if (worldState.hasFlag("civic_reformer")) {
            newStatus = "Civic Reformer";

        } else if (worldState.hasFlag("commoner_leader")) {
            newStatus = "Commoner Leader";

            // Normal stat/faction-based statuses
        } else if (player.getPoliticalPower() >= 75 && factions.getCourt() >= 65) {
            newStatus = "Powerful Court Figure";

        } else if (player.getPoliticalPower() >= 65 && factions.getMilitary() >= 70) {
            newStatus = "Military Commander";

        } else if (player.getEducation() >= 75 && factions.getScholars() >= 65) {
            newStatus = "Respected Scholar";

        } else if (player.getWealth() >= 70 && factions.getMerchants() >= 65) {
            newStatus = "Influential Merchant";

        } else if (factions.getShadowNetwork() >= 70 && player.getPoliticalPower() >= 45) {
            newStatus = "Shadow Broker";

        } else if (factions.getCourt() <= 20 && player.getPoliticalPower() >= 40) {
            newStatus = "Enemy of the Court";

        } else if (player.getWealth() <= 15 && player.getStress() >= 70) {
            newStatus = "Desperate Survivor";

        } else if (player.getReputation() >= 55 && factions.getCommonPeople() >= 60) {
            newStatus = "Local Hero";

        } else if (player.getEducation() >= 60) {
            newStatus = "Educated Aspirant";

        } else if (player.getPoliticalPower() >= 45) {
            newStatus = "Rising Political Actor";

        } else if (player.getWealth() >= 45) {
            newStatus = "Stable Householder";

        } else {
            newStatus = player.getOrigin();
        }

        player.setCurrentStatus(newStatus);

        if (!oldStatus.equals(newStatus)) {
            latestStatusChangeMessage =
                    "Your place in society has changed.\n\n"
                            + "From: " + oldStatus + "\n"
                            + "To: " + newStatus + "\n\n"
                            + "This means new events, risks, and opportunities may now appear.";
        }
    }

    public boolean hasMoreEvents() {
        return getCurrentEvent() != null;
    }

    public boolean canChoose(Choice choice) {
        return getLockedReason(choice).isEmpty();
    }

    public String getLockedReason(Choice choice) {
        for (ChoiceRequirement requirement : choice.getRequirements()) {
            if (!requirementMet(requirement)) {
                return requirement.getFailMessage();
            }
        }

        return "";
    }

    public String getLifeSummary() {
        return "\n\n----- Life Summary -----\n"
                + "Age: " + player.getAge() + "\n"
                + "Origin: " + player.getOrigin() + "\n"
                + "Final Status: " + player.getCurrentStatus() + "\n"
                + "Family Condition: " + player.getFamilyCondition() + "\n"
                + "Trait: " + player.getTrait() + "\n"
                + "Legacy Titles: " + player.getLegacyTitlesText() + "\n\n"
                + "Major Memories:\n"
                + getMajorMemoriesText();
    }

    private String getMajorMemoriesText() {
        List<String> memories = new ArrayList<>();

        if (worldState.hasFlag("protected_commoners")) {
            memories.add("You stood with common people during hardship.");
        }

        if (worldState.hasFlag("used_bribery")) {
            memories.add("You once solved a crisis through bribery, and that choice followed you.");
        }

        if (worldState.hasFlag("angered_scholars")) {
            memories.add("Your conflict with scholars damaged your learned reputation.");
        }

        if (worldState.hasFlag("learned_palace_secrets")) {
            memories.add("You learned early that court secrets could become power.");
        }

        if (worldState.hasFlag("betrayed_comrade")) {
            memories.add("You gained attention by betraying someone close to you.");
        }

        if (worldState.hasFlag("used_shadow_contacts")) {
            memories.add("You used hidden contacts to protect your interests.");
        }

        if (worldState.hasFlag("sold_palace_secret")) {
            memories.add("You sold a palace secret and invited danger into your future.");
        }

        if (worldState.hasFlag("declared_loyalty")) {
            memories.add("You publicly supported the ruling dynasty.");
        }

        if (worldState.hasFlag("heard_rebel_voices")) {
            memories.add("You listened to voices of rebellion when the dynasty weakened.");
        }

        if (worldState.hasFlag("fed_people_during_riot")) {
            memories.add("You gave relief to hungry people during unrest.");
        }

        if (worldState.hasFlag("fair_scholar_judgment")) {
            memories.add("You were remembered for a fair judgment.");
        }

        if (worldState.hasFlag("disciplined_soldiers")) {
            memories.add("You commanded soldiers with discipline.");
        }

        if (worldState.hasFlag("protected_merchants")) {
            memories.add("You protected merchants from unfair officials.");
        }

        if (worldState.hasFlag("served_shadow_network")) {
            memories.add("You served the shadow network when they demanded payment.");
        }

        if (!player.getOrigin().equals(player.getCurrentStatus())) {
            memories.add("Your life changed your social position from your original background.");
        }

        if (player.getLegacyTitlesText().equals("None")) {
            memories.add("You gained no famous legacy title, but your household still carried the memory of your choices.");
        }

        if (memories.isEmpty()) {
            memories.add("Your life passed without one famous public turning point, but your choices still shaped your household.");
        }

        return "- " + String.join("\n- ", memories);
    }

    public EndingResult getEndingResult() {
        if (endingResult == null) {
            endingResult = calculateEnding();
        }

        return endingResult;
    }

    public String consumeLatestLegacyTitleMessage() {
        String message = latestLegacyTitleMessage;
        latestLegacyTitleMessage = "";
        return message;
    }

    private boolean requirementMet(ChoiceRequirement requirement) {
        return switch (requirement.getType()) {
            case "statMin" -> player.getStatValue(requirement.getKey()) >= requirement.getMinValue();
            case "factionMin" -> getFactionValue(requirement.getKey()) >= requirement.getMinValue();
            case "familyIs" -> player.getFamilyCondition().equals(requirement.getExpectedValue());
            case "familyNot" -> !player.getFamilyCondition().equals(requirement.getExpectedValue());
            case "originIs" -> player.getOrigin().equals(requirement.getExpectedValue());
            case "title" -> player.hasLegacyTitle(requirement.getExpectedValue());
            default -> true;
        };
    }

    private EndingResult calculateDeathEnding(String titles) {

        String endingInfo =
                "\n\nAge at death: " + player.getAge()
                        + "\nOrigin: " + player.getOrigin()
                        + "\nFinal Status: " + player.getCurrentStatus()
                        + "\n\nLegacy Titles: " + titles;

        if (player.getAge() >= 85) {
            return new EndingResult(
                    "Old Age Final Chapter",
                    "Your life reached a rare old age. By the end, your body grew weak, but your household had years to remember your choices, warnings, victories, and mistakes."
                            + endingInfo
            );
        }

        if (player.getAge() >= 70 && player.getHealth() <= 30) {
            return new EndingResult(
                    "Final Years of Decline",
                    "Age and poor health slowly closed your path. You survived many storms, but your final years were shaped by weakness, memory, and the consequences of earlier decisions."
                            + endingInfo
            );
        }

        if (worldState.hasFlag("court_suspicion") && factions.getCourt() <= 30) {
            return new EndingResult(
                    "Died Under Court Suspicion",
                    "The court never fully trusted you again. Suspicion followed your name until your final days, and your household learned that survival near power always has a price."
                            + endingInfo
            );
        }

        if (worldState.hasFlag("collector_reported_you") && factions.getCourt() <= 35) {
            return new EndingResult(
                    "Destroyed by Corruption",
                    "The old bribe returned at the worst possible time. The report damaged your name, weakened your protection, and left your family carrying the cost of your hidden mistake."
                            + endingInfo
            );
        }

        if (worldState.hasFlag("owed_shadow_debt") && factions.getShadowNetwork() >= 70) {
            return new EndingResult(
                    "Owned by Shadows",
                    "The hidden world protected you, but that protection became a chain. By the end, your fate belonged more to secret allies than to your own household."
                            + endingInfo
            );
        }

        if (worldState.hasFlag("framed_innocent") && player.getMorality() <= 25) {
            return new EndingResult(
                    "Buried by Lies",
                    "You survived by pushing blame onto another person, but lies rarely stay quiet forever. Your final chapter was shaped by fear, suspicion, and the weight of what you chose."
                            + endingInfo
            );
        }

        if (player.getWealth() <= 10 && player.getStress() >= 75) {
            return new EndingResult(
                    "Final Years in Poverty",
                    "Poverty followed you until the end. Your household survived on little, and your final days became a reminder that not every legacy is built from power or wealth."
                            + endingInfo
            );
        }

        if (player.getHealth() <= 15 && player.getStress() >= 80) {
            return new EndingResult(
                    "Broken by Hardship",
                    "Years of pressure, poor health, and difficult choices wore you down. Your life ended before your ambitions could fully settle into legacy."
                            + endingInfo
            );
        }

        if (player.getHealth() <= 20) {
            return new EndingResult(
                    "Weak Body, Heavy Life",
                    "Your body could no longer carry the burden of your choices. Even without glory, your struggle became part of your household's memory."
                            + endingInfo
            );
        }

        if (player.getStress() >= 90) {
            return new EndingResult(
                    "Crushed by Pressure",
                    "The pressure around you never truly lifted. Politics, family, fear, and survival weighed on you until your final chapter closed too soon."
                            + endingInfo
            );
        }

        return new EndingResult(
                "Life Cut Short",
                player.getDeathReason()
                        + endingInfo
        );
    }

    private EndingResult calculateEnding() {

        String titles = player.getLegacyTitlesText();

        if (!player.isAlive()) {
            return calculateDeathEnding(titles);
        }

        if (player.getHealth() <= 15) {
            return new EndingResult(
                    "Fading Legend",
                    "Your body could no longer carry the weight of your choices. Yet your name did not vanish completely. Those who remember you speak of a life burned down by struggle, pressure, and survival.\n\nLegacy Titles: " + titles
            );
        }

        if (worldState.hasFlag("collector_reported_you")
                && factions.getCourt() <= 30) {
            return new EndingResult(
                    "Disgraced by Corruption",
                    "The collector's report reached the court before you could silence it. Your old bribe became proof of dishonor. Officials used your corruption to weaken your household, and your bloodline carried the stain for years.\n\nLegacy Titles: " + titles
            );
        }

        if (worldState.hasFlag("court_suspicion")
                && factions.getCourt() <= 35) {
            return new EndingResult(
                    "Watched by the Court",
                    "You survived, but not freely. Court officials kept your name in their private records. Every promotion, meeting, and alliance came under quiet suspicion. You were never fully trusted again.\n\nLegacy Titles: " + titles
            );
        }

        if ((worldState.hasFlag("owed_shadow_debt")
                || worldState.hasFlag("sold_palace_secret")
                || worldState.hasFlag("used_shadow_contacts"))
                && factions.getShadowNetwork() >= 70) {
            return new EndingResult(
                    "Owned by Shadows",
                    "The hidden world protected you, but protection became ownership. You gained influence through whispers, secrets, and fear. In the end, your legacy belonged less to your family and more to the people who knew your darkest choices.\n\nLegacy Titles: " + titles
            );
        }

        if (worldState.hasFlag("angered_scholars")
                && factions.getScholars() <= 25) {
            return new EndingResult(
                    "Enemy of the Learned",
                    "The scholars never forgot your insult. Their letters, sermons, and records slowly shaped your reputation. Even when you gained power, educated circles remembered you as arrogant and unworthy of trust.\n\nLegacy Titles: " + titles
            );
        }

        if (worldState.hasFlag("declared_loyalty")
                && factions.getCourt() >= 65
                && player.getPoliticalPower() >= 55) {
            return new EndingResult(
                    "Pillar of the Dynasty",
                    "You stood with the ruling dynasty when others hesitated. Your loyalty brought you influence, protection, and enemies. To loyalists, you became a pillar of order. To rebels, you became part of the old machine.\n\nLegacy Titles: " + titles
            );
        }

        if (worldState.hasFlag("protected_commoners")
                && factions.getCommonPeople() >= 65
                && player.getReputation() >= 55) {
            return new EndingResult(
                    "Voice of the People",
                    "Common people remembered your courage long after officials tried to reduce it to troublemaking. Your name survived in markets, villages, and family stories as someone who stood beside the weak.\n\nLegacy Titles: " + titles
            );
        }

        if (player.getEducation() >= 75
                && factions.getScholars() >= 65) {
            return new EndingResult(
                    "Honored Scholar",
                    "Knowledge became your strongest inheritance. Scholars preserved your judgments, writings, and advice. Your power was not only in wealth or soldiers, but in the respect of those who shaped memory.\n\nLegacy Titles: " + titles
            );
        }

        if (player.getWealth() >= 75
                && factions.getMerchants() >= 65) {
            return new EndingResult(
                    "Wealthy Elder",
                    "You secured property, trade, and comfort for your household. Some praised your discipline. Others wondered what you sacrificed to gather so much. Your descendants inherited stability, but also expectation.\n\nLegacy Titles: " + titles
            );
        }

        if (player.getFamilyLoyalty() >= 80
                && factions.getFamilyCouncil() >= 65) {
            return new EndingResult(
                    "Guardian of the Bloodline",
                    "You placed family above ambition. Your household survived because you protected it from hunger, shame, and political storms. Your legacy lived first in the people who carried your name.\n\nLegacy Titles: " + titles
            );
        }

        if (player.getPoliticalPower() >= 75
                && player.getReputation() >= 55) {
            return new EndingResult(
                    "Rising Power",
                    "By the end of your life, few could ignore you. You had enemies, allies, and a name that moved through halls of influence. Whether loved or feared, you were no longer insignificant.\n\nLegacy Titles: " + titles
            );
        }

        if (player.getMorality() <= 25
                && player.getPoliticalPower() >= 55) {
            return new EndingResult(
                    "Feared Survivor",
                    "You survived by making choices others feared to make. Mercy did not define your path. People obeyed you, avoided you, and remembered you with caution rather than love.\n\nLegacy Titles: " + titles
            );
        }

        if (player.getStress() >= 85) {
            return new EndingResult(
                    "Burdened Survivor",
                    "You reached the end, but peace rarely found you. Your life was shaped by pressure, danger, and difficult compromises. Survival itself became your final achievement.\n\nLegacy Titles: " + titles
            );
        }

        return new EndingResult(
                "Forgotten Survivor",
                "You lived through your age without becoming a hero, villain, ruler, or martyr. History did not carve your name into stone, but your choices still shaped the quiet future of your household.\n\nLegacy Titles: " + titles
        );
    }

    private int getFactionValue(String faction) {
        return switch (faction) {
            case "court" -> factions.getCourt();
            case "nobles" -> factions.getNobles();
            case "military" -> factions.getMilitary();
            case "scholars" -> factions.getScholars();
            case "merchants" -> factions.getMerchants();
            case "commonPeople" -> factions.getCommonPeople();
            case "familyCouncil" -> factions.getFamilyCouncil();
            case "shadowNetwork" -> factions.getShadowNetwork();
            default -> 0;
        };
    }
}