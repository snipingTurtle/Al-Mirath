package com.example.al_mirath.service;

import com.example.al_mirath.model.EndingResult;
import com.example.al_mirath.model.PlayerCharacter;

/**
 * Decides which achievements a run has earned.
 *
 * <p>Keeping the conditions here rather than scattered through the controller
 * means the rules are all visible in one place and the UI only has to say
 * "a choice happened" or "a life ended".
 */
public final class AchievementEvaluator {

    private AchievementEvaluator() {
    }

    /** Runs after every applied choice. */
    public static void afterChoice(GameEngine engine) {
        if (engine == null) {
            return;
        }

        ProgressService.increment(ProgressService.STAT_CHOICES_MADE, 1);
        ProgressService.unlock(AchievementLibrary.FIRST_CHOICE);

        PlayerCharacter player = engine.getPlayer();
        String status = player.getCurrentStatus();

        ProgressService.unlockIf("Respected Scholar".equals(status), AchievementLibrary.RESPECTED_SCHOLAR);
        ProgressService.unlockIf("Influential Merchant".equals(status), AchievementLibrary.INFLUENTIAL_MERCHANT);
        ProgressService.unlockIf("Military Commander".equals(status), AchievementLibrary.MILITARY_COMMANDER);
        ProgressService.unlockIf("Shadow Broker".equals(status), AchievementLibrary.SHADOW_BROKER);
        ProgressService.unlockIf("Ruler".equals(status), AchievementLibrary.TOOK_THRONE);
        ProgressService.unlockIf("Royal Regent".equals(status), AchievementLibrary.BECAME_REGENT);
    }

    /** Runs after a Threads of Fate challenge resolves. */
    public static void afterMiniGame(GameEngine engine, String gameTitle, boolean won) {
        if (won) {
            ProgressService.increment(ProgressService.STAT_REWINDS_WON, 1);
            ProgressService.unlock(AchievementLibrary.FIRST_REWIND);

            if (gameTitle != null) {
                switch (gameTitle) {
                    case "The Scribe's Hand" -> ProgressService.unlock(AchievementLibrary.SCRIBE_MASTER);
                    case "The Merchant's Scales" -> ProgressService.unlock(AchievementLibrary.MERCHANT_MASTER);
                    case "The Night Courier" -> ProgressService.unlock(AchievementLibrary.COURIER_MASTER);
                    default -> {
                        // No per-game unlock for anything added later.
                    }
                }
            }

            if (engine != null && engine.getMinigamesWon() >= 3) {
                ProgressService.unlock(AchievementLibrary.THREAD_UNBROKEN);
            }

        } else {
            ProgressService.increment(ProgressService.STAT_REWINDS_LOST, 1);
        }
    }

    /**
     * Runs once when a life reaches its ending.
     *
     * @param score the run's final score, used for the personal best counter
     */
    public static void afterLifeComplete(GameEngine engine, EndingResult ending, int score) {
        if (engine == null) {
            return;
        }

        PlayerCharacter player = engine.getPlayer();

        ProgressService.increment(ProgressService.STAT_LIVES_COMPLETED, 1);
        ProgressService.recordMaximum(ProgressService.STAT_OLDEST_AGE, player.getAge());
        ProgressService.recordMaximum(ProgressService.STAT_HIGHEST_SCORE, score);
        ProgressService.recordEraCompleted(player.getEra());

        int titles = player.getLegacyTitles().size();
        ProgressService.increment(ProgressService.STAT_TITLES_EARNED, titles);

        ProgressService.unlock(AchievementLibrary.FIRST_LIFE);

        int lives = ProgressService.statistic(ProgressService.STAT_LIVES_COMPLETED);
        ProgressService.unlockIf(lives >= 5, AchievementLibrary.FIVE_LIVES);
        ProgressService.unlockIf(lives >= 20, AchievementLibrary.TWENTY_LIVES);

        int totalTitles = ProgressService.statistic(ProgressService.STAT_TITLES_EARNED);
        ProgressService.unlockIf(totalTitles >= 5, AchievementLibrary.FIVE_TITLES);
        ProgressService.unlockIf(totalTitles >= 10, AchievementLibrary.TEN_TITLES);

        ProgressService.unlockIf(ProgressService.erasCompleted() >= 4, AchievementLibrary.ALL_ERAS);

        ProgressService.unlockIf(player.getMorality() >= 90, AchievementLibrary.SAINT);
        ProgressService.unlockIf(player.getMorality() <= 10, AchievementLibrary.TYRANT);

        ProgressService.unlockIf(player.getAge() >= 85, AchievementLibrary.OLD_AGE);
        ProgressService.unlockIf(!player.isAlive() && player.getAge() < 25, AchievementLibrary.DIED_YOUNG);

        ProgressService.unlockIf(titles == 0, AchievementLibrary.FORGOTTEN);

        ProgressService.unlockIf(
                ending != null && ending.getTitle().contains("Shadows"),
                AchievementLibrary.OWNED_BY_SHADOWS
        );

        // Threads were available all run; finishing without spending one is a
        // deliberate show of confidence.
        ProgressService.unlockIf(engine.getRewindsUsed() == 0
                && engine.getMinigamesLost() == 0, AchievementLibrary.NO_REGRETS);

        ProgressService.unlockIf(engine.playedCleanRun(), AchievementLibrary.INCORRUPTIBLE);
    }
}
