package controller;

import game.UnoGame;
import history.CompletedGame;
import history.CompletedRound;
import history.PlayerResult;
import history.PlayerScore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ui.UiView;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

final class GameSessionController {

    private static final Logger LOGGER = LoggerFactory.getLogger(GameSessionController.class);

    private final UnoGame game;
    private final UiView view;
    private final TurnController turnController;
    private final Clock clock;

    GameSessionController(UnoGame game, UiView view, TurnController turnController, Clock clock) {
        this.game = game;
        this.view = view;
        this.turnController = turnController;
        this.clock = clock;
    }

    void setupGame(GameSettings settings) {
        game.seedRandom(settings.seed());
        game.setupPlayers(settings.bots(), settings.human());
    }

    boolean isPlayerCountValid() {
        return game.isPlayerCountValid();
    }

    CompletedGame playGames(GameSettings settings) {
        Instant sessionStartedAt = clock.instant();
        List<CompletedRound> completedRounds = new ArrayList<>();

        for (int gameCount = 1; gameCount <= settings.games(); gameCount++) {
            LOGGER.info("event=game_start game={}", gameCount);
            view.showGameHeader(gameCount);
            completedRounds.add(playRound(gameCount));
            if (settings.hasTargetScore() && game.hasPlayerReachedScore(settings.targetScore())) {
                break;
            }
        }

        return new CompletedGame(
                sessionStartedAt,
                clock.instant(),
                settings.games(),
                playerResults(),
                completedRounds
        );
    }

    private CompletedRound playRound(int roundNumber) {
        Instant roundStartedAt = clock.instant();
        int[] scoresBefore = game.scoresSnapshot();
        RoundOutcome outcome = playGame();
        Instant roundCompletedAt = clock.instant();

        return new CompletedRound(
                roundNumber,
                roundStartedAt,
                roundCompletedAt,
                outcome.status(),
                outcome.winnerName(),
                outcome.pointsAwarded(),
                playerScores(scoresBefore, game.scoresSnapshot())
        );
    }

    private RoundOutcome playGame() {
        game.startRound();

        int guard = 0;
        while (guard < 3000) {
            guard++;
            Optional<RoundOutcome> outcome = turnController.playCurrentTurn();
            if (outcome.isPresent()) {
                return outcome.orElseThrow();
            }
        }

        LOGGER.info("event=round_end result=safety_limit");
        view.showSafetyLimitReached();
        return RoundOutcome.safetyLimit();
    }

    void showFinalScores() {
        var playerNames = game.playerNamesSnapshot();
        LOGGER.info("event=session_end players={}", playerNames.size());
        view.showFinalScores(playerNames, game.scoresSnapshot());
    }

    private List<PlayerScore> playerScores(int[] before, int[] after) {
        List<String> playerNames = game.playerNamesSnapshot();
        List<PlayerScore> scores = new ArrayList<>(playerNames.size());
        for (int player = 0; player < playerNames.size(); player++) {
            scores.add(new PlayerScore(
                    playerNames.get(player),
                    before[player],
                    after[player] - before[player],
                    after[player]
            ));
        }
        return scores;
    }

    private List<PlayerResult> playerResults() {
        List<String> playerNames = game.playerNamesSnapshot();
        int[] scores = game.scoresSnapshot();
        int highestScore = 0;
        for (int player = 0; player < playerNames.size(); player++) {
            highestScore = Math.max(highestScore, scores[player]);
        }

        List<PlayerResult> results = new ArrayList<>(playerNames.size());
        for (int player = 0; player < playerNames.size(); player++) {
            results.add(new PlayerResult(
                    playerNames.get(player),
                    scores[player],
                    scores[player] == highestScore
            ));
        }
        return results;
    }
}
