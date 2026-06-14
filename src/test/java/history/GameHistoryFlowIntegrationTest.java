package history;

import controller.UnoGameController;
import history.persistence.TestJpaApplication;
import history.persistence.entity.GameEntity;
import history.persistence.entity.GamePlayerEntity;
import history.persistence.entity.RoundStatus;
import history.persistence.repository.GameRepository;
import history.persistence.repository.PlayerRepository;
import history.persistence.repository.RoundScoreRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import ui.UiType;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest(showSql = false)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@ContextConfiguration(classes = TestJpaApplication.class)
@Import(GameHistoryService.class)
final class GameHistoryFlowIntegrationTest {

    @Autowired
    private GameHistoryService gameHistoryService;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private RoundScoreRepository roundScoreRepository;

    @Test
    void persistsDeterministicGameSessionThroughControllerFlow() {
        String output = captureOutput(() -> UnoGameController.startNewGame(
                new UiType.Cli(new String[]{
                        "--bots", "3",
                        "--games", "5",
                        "--quiet",
                        "--seed", "123"
                }),
                gameHistoryService
        ));

        GameEntity game = gameRepository
                .findAllByOrderByCompletedAtDescIdDesc(PageRequest.of(0, 1))
                .getFirst();
        Map<String, GamePlayerEntity> players = game.getPlayers().stream()
                .collect(Collectors.toMap(
                        gamePlayer -> gamePlayer.getPlayer().getDisplayName(),
                        gamePlayer -> gamePlayer
                ));
        String recentGames = captureOutput(() -> runReport("--recent-games", "1"));
        String playerWins = captureOutput(() -> runReport("--player-wins", "bot2"));
        String highestScores = captureOutput(() -> runReport("--highest-scores", "3"));

        assertAll(
                () -> assertTrue(output.contains("Bot1: 138")),
                () -> assertTrue(output.contains("Bot2: 246")),
                () -> assertTrue(output.contains("Bot3: 98")),
                () -> assertEquals(1, gameRepository.count()),
                () -> assertEquals(3, playerRepository.count()),
                () -> assertEquals(5, game.getRequestedRounds()),
                () -> assertEquals(5, game.getCompletedRounds()),
                () -> assertEquals(5, game.getRounds().size()),
                () -> assertEquals(15, roundScoreRepository.count()),
                () -> assertTrue(game.getRounds().stream()
                        .allMatch(round -> round.getStatus() == RoundStatus.COMPLETED)),
                () -> assertTrue(game.getRounds().stream()
                        .allMatch(round -> round.getWinner() != null)),
                () -> assertEquals(482, game.getRounds().stream()
                        .mapToInt(round -> round.getPointsAwarded())
                        .sum()),
                () -> assertEquals(138, players.get("Bot1").getFinalScore()),
                () -> assertEquals(246, players.get("Bot2").getFinalScore()),
                () -> assertEquals(98, players.get("Bot3").getFinalScore()),
                () -> assertEquals(138, scoreDeltasFor(game, players.get("Bot1"))),
                () -> assertEquals(246, scoreDeltasFor(game, players.get("Bot2"))),
                () -> assertEquals(98, scoreDeltasFor(game, players.get("Bot3"))),
                () -> assertTrue(players.get("Bot2").isWinner()),
                () -> assertEquals(1, players.values().stream()
                        .filter(GamePlayerEntity::isWinner)
                        .count()),
                () -> assertTrue(recentGames.contains("Recent games:")),
                () -> assertTrue(recentGames.contains("winner: Bot2")),
                () -> assertTrue(recentGames.contains("Bot1=138, Bot2=246, Bot3=98")),
                () -> assertTrue(playerWins.contains("bot2 wins: 1")),
                () -> assertTrue(highestScores.contains("Highest scores:")),
                () -> assertTrue(highestScores.contains("Bot2: 246"))
        );
    }

    private void runReport(String... args) {
        UnoGameController.startNewGame(
                new UiType.Cli(args),
                GameHistoryWriter.noOp(),
                gameHistoryService
        );
    }

    private int scoreDeltasFor(GameEntity game, GamePlayerEntity gamePlayer) {
        return game.getRounds().stream()
                .flatMap(round -> round.getScores().stream())
                .filter(score -> score.getGamePlayer().getId().equals(gamePlayer.getId()))
                .mapToInt(score -> score.getScoreDelta())
                .sum();
    }

    private String captureOutput(Runnable action) {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8));
        try {
            action.run();
        } finally {
            System.setOut(originalOut);
        }
        return output.toString(StandardCharsets.UTF_8);
    }
}
