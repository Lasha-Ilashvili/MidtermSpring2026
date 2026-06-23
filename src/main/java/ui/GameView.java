package ui;

import java.util.List;

public interface GameView {

    void setQuiet(boolean quiet);

    void showTurn(String playerName, List<String> hand, String upCard, String calledColor);

    void showCardDrawn(String playerName, String card);

    void showInvalidIndexPenalty(String playerName);

    void showIllegalCardPenalty(String playerName, String card);

    void showCardPlayed(String playerName, String card);

    void showColorCalled(String playerName, String color);

    void showUno(String playerName);

    void showMissedUnoPenalty(String playerName);

    void showWinnerScore(String playerName, int points);

    void showDrawTwoPenalty(String playerName);

    void showDrawFourPenalty(String playerName);

    void showSafetyLimitReached();

    void showChooseCardPrompt();

    void showCardNotLegal();

    void showCardNotFound();

    void showCallColorPrompt();

    void showBadColor();

    void showPlayDrawnCardPrompt(String card);

    void showUsage();

    void showInvalidPlayerCount();

    void showUnsupportedUiType();

    void showGameHeader(int gameCount);

    void showFinalScores(List<String> playerNames, int[] scores);
}
