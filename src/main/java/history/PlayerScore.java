package history;

import java.util.Objects;

public record PlayerScore(String playerName, int scoreBefore, int scoreDelta, int scoreAfter) {

    public PlayerScore {
        Objects.requireNonNull(playerName);
    }
}
