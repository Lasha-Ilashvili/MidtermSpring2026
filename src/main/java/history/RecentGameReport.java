package history;

import java.time.Instant;
import java.util.List;

public record RecentGameReport(
        long gameId,
        Instant completedAt,
        int roundsPlayed,
        List<GamePlayerReport> players
) {

    public RecentGameReport {
        players = List.copyOf(players);
    }
}
