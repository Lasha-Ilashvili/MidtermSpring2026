package controller;

import history.GameHistoryReader;
import ui.Startup;
import ui.UiView;

final class HistoryReportController {

    private static final int DEFAULT_LIMIT = 10;
    private static final int MAX_LIMIT = 100;

    private final GameHistoryReader gameHistoryReader;
    private final UiView view;

    HistoryReportController(GameHistoryReader gameHistoryReader, UiView view) {
        this.gameHistoryReader = gameHistoryReader;
        this.view = view;
    }

    boolean handle(Startup.Input startupInput) {
        if (startupInput.action() != Startup.Action.SHOW_REPORT) {
            return false;
        }

        Startup.Report report = startupInput.report();
        switch (report.type()) {
            case RECENT_GAMES -> showRecentGames(report.value());
            case PLAYER_WINS -> showPlayerWins(report.value());
            case HIGHEST_SCORES -> showHighestScores(report.value());
        }
        return true;
    }

    private void showRecentGames(String rawLimit) {
        Integer limit = parseLimit(rawLimit);
        if (limit != null) {
            view.showRecentGames(gameHistoryReader.recentGames(limit));
        }
    }

    private void showPlayerWins(String playerName) {
        if (playerName == null || playerName.isBlank()) {
            view.showInvalidReportArguments();
            return;
        }
        view.showPlayerWinCount(playerName, gameHistoryReader.playerWinCount(playerName));
    }

    private void showHighestScores(String rawLimit) {
        Integer limit = parseLimit(rawLimit);
        if (limit != null) {
            view.showHighestScores(gameHistoryReader.highestScores(limit));
        }
    }

    private Integer parseLimit(String rawLimit) {
        if (rawLimit == null) {
            return DEFAULT_LIMIT;
        }

        try {
            int limit = Integer.parseInt(rawLimit);
            if (limit >= 1 && limit <= MAX_LIMIT) {
                return limit;
            }
        } catch (NumberFormatException ignored) {
        }
        view.showInvalidReportArguments();
        return null;
    }
}
