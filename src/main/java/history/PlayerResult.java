package history;

import java.util.Objects;

public record PlayerResult(String name, int finalScore, boolean winner) {

    public PlayerResult {
        Objects.requireNonNull(name);
    }
}
