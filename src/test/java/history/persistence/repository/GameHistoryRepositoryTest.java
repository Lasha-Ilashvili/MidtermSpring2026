package history.persistence.repository;

import history.persistence.TestJpaApplication;
import history.persistence.entity.GameEntity;
import history.persistence.entity.GamePlayerEntity;
import history.persistence.entity.PlayerEntity;
import history.persistence.entity.RoundEntity;
import history.persistence.entity.RoundScoreEntity;
import history.persistence.entity.RoundStatus;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.time.Instant;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest(showSql = false)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@ContextConfiguration(classes = TestJpaApplication.class)
final class GameHistoryRepositoryTest {

    @Autowired
    private Flyway flyway;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private GamePlayerRepository gamePlayerRepository;

    @Autowired
    private RoundRepository roundRepository;

    @Autowired
    private RoundScoreRepository roundScoreRepository;

    @Test
    void appliesMigrationAndPersistsCompleteGameGraph() {
        assertEquals("1", flyway.info().current().getVersion().getVersion());

        GameEntity game = saveGame(
                Instant.parse("2026-06-15T10:00:00Z"),
                List.of(new PlayerResult("Alice", 75, true), new PlayerResult("Bob", 20, false))
        );

        assertAll(
                () -> assertNotNull(game.getId()),
                () -> assertEquals(2, playerRepository.count()),
                () -> assertEquals(1, gameRepository.count()),
                () -> assertEquals(2, gamePlayerRepository.count()),
                () -> assertEquals(1, roundRepository.count()),
                () -> assertEquals(2, roundScoreRepository.count()),
                () -> assertEquals("Alice", game.getRounds().getFirst().getWinner().getPlayer().getDisplayName()),
                () -> assertEquals(75, game.getRounds().getFirst().getPointsAwarded())
        );
    }

    @Test
    void startsEachRepositoryTestWithRolledBackState() {
        assertAll(
                () -> assertEquals(0, playerRepository.count()),
                () -> assertEquals(0, gameRepository.count()),
                () -> assertEquals(0, gamePlayerRepository.count()),
                () -> assertEquals(0, roundRepository.count()),
                () -> assertEquals(0, roundScoreRepository.count())
        );
    }

    @Test
    void executesDerivedAndAnnotatedReportQueries() {
        saveGame(
                Instant.parse("2026-06-15T10:00:00Z"),
                List.of(new PlayerResult("Alice", 75, true), new PlayerResult("Bob", 20, false))
        );
        saveGame(
                Instant.parse("2026-06-15T12:00:00Z"),
                List.of(new PlayerResult("Alice", 40, false), new PlayerResult("Bob", 90, true))
        );

        List<GameEntity> recent = gameRepository.findAllByOrderByCompletedAtDesc(PageRequest.of(0, 1));
        List<GamePlayerEntity> highest = gamePlayerRepository.findHighestScores(PageRequest.of(0, 3));

        assertAll(
                () -> assertEquals(1, gamePlayerRepository.countByPlayerNormalizedNameAndWinnerTrue("alice")),
                () -> assertEquals(1, gamePlayerRepository.countByPlayerNormalizedNameAndWinnerTrue("bob")),
                () -> assertEquals(Instant.parse("2026-06-15T12:00:00Z"), recent.getFirst().getCompletedAt()),
                () -> assertEquals(List.of(90, 75, 40),
                        highest.stream().map(GamePlayerEntity::getFinalScore).toList()),
                () -> assertTrue(playerRepository.findByNormalizedName("alice").isPresent())
        );
    }

    private GameEntity saveGame(Instant completedAt, List<PlayerResult> results) {
        Instant startedAt = completedAt.minusSeconds(60);
        GameEntity game = new GameEntity(startedAt, completedAt, 1, 1);

        for (int index = 0; index < results.size(); index++) {
            PlayerResult result = results.get(index);
            String normalizedName = result.name().toLowerCase(Locale.ROOT);
            PlayerEntity player = playerRepository.findByNormalizedName(normalizedName)
                    .orElseGet(() -> playerRepository.save(
                            new PlayerEntity(result.name(), normalizedName)
                    ));
            game.addPlayer(new GamePlayerEntity(player, index, result.score(), result.winner()));
        }

        GamePlayerEntity roundWinner = game.getPlayers().stream()
                .filter(GamePlayerEntity::isWinner)
                .findFirst()
                .orElseThrow();
        RoundEntity round = new RoundEntity(
                1,
                startedAt,
                completedAt,
                RoundStatus.COMPLETED,
                roundWinner,
                roundWinner.getFinalScore()
        );

        for (GamePlayerEntity gamePlayer : game.getPlayers()) {
            round.addScore(new RoundScoreEntity(
                    gamePlayer,
                    0,
                    gamePlayer.getFinalScore(),
                    gamePlayer.getFinalScore()
            ));
        }
        game.addRound(round);

        return gameRepository.saveAndFlush(game);
    }

    private record PlayerResult(String name, int score, boolean winner) {
    }
}
