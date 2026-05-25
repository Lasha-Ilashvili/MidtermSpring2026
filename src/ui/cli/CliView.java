package ui.cli;

import java.util.List;
import java.util.function.Supplier;
import ui.PlayerInput;
import ui.Startup;
import ui.UiType;
import ui.UiView;

public class CliView implements UiView {

    private final CliStartupInputReader startupInputReader = new CliStartupInputReader();
    private final CliPlayerInputReader playerInputReader = new CliPlayerInputReader();
    private final CliGameView gameView = new CliGameView();

    @Override
    public void setQuiet(boolean quiet) {
        gameView.setQuiet(quiet);
    }

    @Override
    public Startup.Input readStartupInput(UiType uiType) {
        return startupInputReader.readStartupInput(uiType);
    }

    public Startup.Input readCliStartupInput(String[] args) {
        return startupInputReader.readCliStartupInput(args);
    }

    @Override
    public void showTurn(String playerName, List<String> hand, String upCard, String calledColor) {
        gameView.showTurn(playerName, hand, upCard, calledColor);
    }

    public String join(List<String> cards) {
        return gameView.join(cards);
    }

    @Override
    public void showCardDrawn(String playerName, String card) {
        gameView.showCardDrawn(playerName, card);
    }

    @Override
    public void showInvalidIndexPenalty(String playerName) {
        gameView.showInvalidIndexPenalty(playerName);
    }

    @Override
    public void showIllegalCardPenalty(String playerName, String card) {
        gameView.showIllegalCardPenalty(playerName, card);
    }

    @Override
    public void showCardPlayed(String playerName, String card) {
        gameView.showCardPlayed(playerName, card);
    }

    @Override
    public void showColorCalled(String playerName, String color) {
        gameView.showColorCalled(playerName, color);
    }

    @Override
    public void showUno(String playerName) {
        gameView.showUno(playerName);
    }

    @Override
    public void showWinnerScore(String playerName, int points) {
        gameView.showWinnerScore(playerName, points);
    }

    @Override
    public void showDrawTwoPenalty(String playerName) {
        gameView.showDrawTwoPenalty(playerName);
    }

    @Override
    public void showDrawFourPenalty(String playerName) {
        gameView.showDrawFourPenalty(playerName);
    }

    @Override
    public void showSafetyLimitReached() {
        gameView.showSafetyLimitReached();
    }

    @Override
    public void showChooseCardPrompt() {
        gameView.showChooseCardPrompt();
    }

    @Override
    public void showCardNotLegal() {
        gameView.showCardNotLegal();
    }

    @Override
    public void showCardNotFound() {
        gameView.showCardNotFound();
    }

    @Override
    public void showCallColorPrompt() {
        gameView.showCallColorPrompt();
    }

    @Override
    public void showBadColor() {
        gameView.showBadColor();
    }

    @Override
    public void showPlayDrawnCardPrompt(String card) {
        gameView.showPlayDrawnCardPrompt(card);
    }

    @Override
    public void showUsage() {
        gameView.showUsage();
    }

    @Override
    public void showInvalidPlayerCount() {
        gameView.showInvalidPlayerCount();
    }

    @Override
    public void showUnsupportedUiType() {
        gameView.showUnsupportedUiType();
    }

    @Override
    public void showGameHeader(int gameCount) {
        gameView.showGameHeader(gameCount);
    }

    @Override
    public void showFinalScores(List<String> playerNames, int[] scores) {
        gameView.showFinalScores(playerNames, scores);
    }

    @Override
    public boolean readPlayDrawnCardDecision() {
        return playerInputReader.readPlayDrawnCardDecision();
    }

    @Override
    public PlayerInput.CardChoice readCardChoice() {
        return playerInputReader.readCardChoice();
    }

    @Override
    public String readColorInput() {
        return playerInputReader.readColorInput();
    }

    public <T> T withInput(String input, Supplier<T> action) {
        return playerInputReader.withInput(input, action);
    }
}
