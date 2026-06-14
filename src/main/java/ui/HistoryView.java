package ui;

import history.HighScoreReport;
import history.RecentGameReport;

import java.util.List;

public interface HistoryView {

    void showRecentGames(List<RecentGameReport> games);

    void showPlayerWinCount(String playerName, long wins);

    void showHighestScores(List<HighScoreReport> scores);

    void showInvalidReportArguments();
}
