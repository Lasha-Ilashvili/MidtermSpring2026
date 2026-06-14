package history;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

public record CompletedGame(
        Instant startedAt,
        Instant completedAt,
        int requestedRounds,
        List<PlayerResult> players,
        List<CompletedRound> rounds
) {

    public CompletedGame {
        Objects.requireNonNull(startedAt);
        Objects.requireNonNull(completedAt);
        players = List.copyOf(players);
        rounds = List.copyOf(rounds);
    }

    public int completedRounds() {
        return rounds.size();
    }
}
