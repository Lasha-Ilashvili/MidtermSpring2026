package history;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

public record CompletedGame(
        Instant startedAt,
        Instant completedAt,
        int requestedRounds,
        Integer targetScore,
        CompletionReason completionReason,
        List<PlayerResult> players,
        List<CompletedRound> rounds
) {

    public CompletedGame {
        Objects.requireNonNull(startedAt);
        Objects.requireNonNull(completedAt);
        Objects.requireNonNull(completionReason);
        players = List.copyOf(players);
        rounds = List.copyOf(rounds);
    }

    public CompletedGame(
            Instant startedAt,
            Instant completedAt,
            int requestedRounds,
            List<PlayerResult> players,
            List<CompletedRound> rounds
    ) {
        this(startedAt, completedAt, requestedRounds, null, CompletionReason.ROUND_CAP, players, rounds);
    }

    public int completedRounds() {
        return rounds.size();
    }

    public enum CompletionReason {
        ROUND_CAP,
        TARGET_SCORE
    }
}
