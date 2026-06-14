package controller;

import history.GameHistoryReader;
import history.GameHistoryWriter;
import history.GamePlayerReport;
import history.HighScoreReport;
import history.RecentGameReport;
import org.junit.jupiter.api.Test;
import ui.UiType;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class HistoryReportCliTest {

    private final OutputCapture output = new OutputCapture();

    @Test
    void usesDefaultAndExplicitReportLimits() {
        RecordingHistoryReader reader = new RecordingHistoryReader();

        runReport(new String[]{"--recent-games"}, reader);
        assertEquals(10, reader.recentLimit);

        runReport(new String[]{"--highest-scores", "3"}, reader);

        assertAll(
                () -> assertEquals(3, reader.highScoreLimit),
                () -> assertTrue(output.contains("Highest scores:")),
                () -> assertTrue(output.contains("Alice: 90"))
        );
    }

    @Test
    void delegatesCasePreservingPlayerWinRequest() {
        RecordingHistoryReader reader = new RecordingHistoryReader();

        runReport(new String[]{"--player-wins", "Alice Smith"}, reader);

        assertAll(
                () -> assertEquals("Alice Smith", reader.playerName),
                () -> assertTrue(output.contains("Alice Smith wins: 4"))
        );
    }

    @Test
    void rejectsInvalidOrMixedReportArgumentsWithoutQueryingHistory() {
        RecordingHistoryReader reader = new RecordingHistoryReader();

        runReport(new String[]{"--highest-scores", "0"}, reader);
        assertTrue(output.contains("Invalid report arguments."));

        runReport(new String[]{"--recent-games", "--bots", "3"}, reader);

        assertAll(
                () -> assertTrue(output.contains("Invalid report arguments.")),
                () -> assertTrue(output.contains("Usage:")),
                () -> assertEquals(0, reader.recentCalls),
                () -> assertEquals(0, reader.highScoreCalls)
        );
    }

    private void runReport(String[] args, RecordingHistoryReader reader) {
        output.capture(() -> {
            UnoGameController.startNewGame(
                    new UiType.Cli(args),
                    GameHistoryWriter.noOp(),
                    reader
            );
            return null;
        });
    }

    private static final class RecordingHistoryReader implements GameHistoryReader {

        private int recentLimit;
        private int highScoreLimit;
        private int recentCalls;
        private int highScoreCalls;
        private String playerName;

        @Override
        public List<RecentGameReport> recentGames(int limit) {
            recentLimit = limit;
            recentCalls++;
            return List.of(new RecentGameReport(
                    1,
                    Instant.parse("2026-06-15T12:00:00Z"),
                    5,
                    List.of(new GamePlayerReport("Alice", 90, true))
            ));
        }

        @Override
        public long playerWinCount(String playerName) {
            this.playerName = playerName;
            return 4;
        }

        @Override
        public List<HighScoreReport> highestScores(int limit) {
            highScoreLimit = limit;
            highScoreCalls++;
            return List.of(new HighScoreReport(
                    1,
                    "Alice",
                    90,
                    Instant.parse("2026-06-15T12:00:00Z")
            ));
        }
    }
}
