package history;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

public record CompletedRound(
        int number,
        Instant startedAt,
        Instant completedAt,
        Status status,
        String winnerName,
        int pointsAwarded,
        List<PlayerScore> scores
) {

    public CompletedRound {
        Objects.requireNonNull(startedAt);
        Objects.requireNonNull(completedAt);
        Objects.requireNonNull(status);
        scores = List.copyOf(scores);
    }

    public enum Status {
        COMPLETED,
        SAFETY_LIMIT
    }
}
