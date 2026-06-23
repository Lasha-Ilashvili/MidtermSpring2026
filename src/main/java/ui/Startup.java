package ui;

public final class Startup {

    private Startup() {
    }

    public enum Action {
        START_GAME,
        SHOW_REPORT,
        HELP,
        INVALID_ARGUMENTS,
        UNSUPPORTED_UI
    }

    public enum ReportType {
        RECENT_GAMES,
        PLAYER_WINS,
        HIGHEST_SCORES
    }

    public record Report(ReportType type, String value) {
    }

    public record Input(
            String bots,
            String games,
            boolean human,
            boolean quiet,
            String seed,
            String targetScore,
            Action action,
            Report report
    ) {
    }

    public static Input unsupportedUi() {
        return new Input(null, null, false, false, null, null, Action.UNSUPPORTED_UI, null);
    }
}
