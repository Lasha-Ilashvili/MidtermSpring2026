package history;

import java.util.List;

public interface GameHistoryReader {

    List<RecentGameReport> recentGames(int limit);

    long playerWinCount(String playerName);

    List<HighScoreReport> highestScores(int limit);

    static GameHistoryReader empty() {
        return new GameHistoryReader() {
            @Override
            public List<RecentGameReport> recentGames(int limit) {
                return List.of();
            }

            @Override
            public long playerWinCount(String playerName) {
                return 0;
            }

            @Override
            public List<HighScoreReport> highestScores(int limit) {
                return List.of();
            }
        };
    }
}
