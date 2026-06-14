package history;

import java.time.Instant;

public record HighScoreReport(
        long gameId,
        String playerName,
        int score,
        Instant completedAt
) {
}
