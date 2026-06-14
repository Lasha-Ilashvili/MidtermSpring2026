package ui;

public final class Startup {

    private Startup() {
    }

    public enum Action {
        START_GAME,
        HELP,
        UNSUPPORTED_UI
    }

    public record Input(
            String bots,
            String games,
            boolean human,
            boolean quiet,
            String seed,
            Action action
    ) {
    }

    public static Input unsupportedUi() {
        return new Input(null, null, false, false, null, Action.UNSUPPORTED_UI);
    }
}
