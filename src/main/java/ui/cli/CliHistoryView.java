package ui.cli;

import history.GamePlayerReport;
import history.HighScoreReport;
import history.RecentGameReport;
import ui.HistoryView;

import java.util.List;
import java.util.stream.Collectors;

final class CliHistoryView implements HistoryView {

    @Override
    public void showRecentGames(List<RecentGameReport> games) {
        if (games.isEmpty()) {
            System.out.println("No games found.");
            return;
        }

        System.out.println("Recent games:");
        for (RecentGameReport game : games) {
            String winners = game.players().stream()
                    .filter(GamePlayerReport::winner)
                    .map(GamePlayerReport::playerName)
                    .collect(Collectors.joining(", "));
            String scores = game.players().stream()
                    .map(player -> player.playerName() + "=" + player.score())
                    .collect(Collectors.joining(", "));
            System.out.printf(
                    "Game %d | %s | rounds: %d | winner: %s | scores: %s%n",
                    game.gameId(),
                    game.completedAt(),
                    game.roundsPlayed(),
                    winners,
                    scores
            );
        }
    }

    @Override
    public void showPlayerWinCount(String playerName, long wins) {
        System.out.println(playerName + " wins: " + wins);
    }

    @Override
    public void showHighestScores(List<HighScoreReport> scores) {
        if (scores.isEmpty()) {
            System.out.println("No scores found.");
            return;
        }

        System.out.println("Highest scores:");
        for (HighScoreReport score : scores) {
            System.out.printf(
                    "%s: %d | game %d | %s%n",
                    score.playerName(),
                    score.score(),
                    score.gameId(),
                    score.completedAt()
            );
        }
    }

    @Override
    public void showInvalidReportArguments() {
        System.out.println("Invalid report arguments.");
    }
}
