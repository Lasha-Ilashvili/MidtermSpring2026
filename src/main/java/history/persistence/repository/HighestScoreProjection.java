package history.persistence.repository;

import java.time.Instant;

public interface HighestScoreProjection {

    Long getGameId();

    String getPlayerName();

    int getScore();

    Instant getCompletedAt();
}
