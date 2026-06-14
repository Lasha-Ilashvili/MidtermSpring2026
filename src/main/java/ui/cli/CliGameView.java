package ui.cli;

import java.util.List;
import ui.GameView;

final class CliGameView implements GameView {

    private boolean quiet = false;

    @Override
    public void setQuiet(boolean quiet) {
        this.quiet = quiet;
    }

    @Override
    public void showTurn(String playerName, List<String> hand, String upCard, String calledColor) {
        if (quiet) {
            return;
        }
        System.out.println("\nUp card: " + upCard + (calledColor.isEmpty() ? "" : " called " + calledColor));
        System.out.println(playerName + " hand: " + join(hand));
    }

    String join(List<String> cards) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < cards.size(); i++) {
            out.append(i).append(":").append(cards.get(i));
            if (i < cards.size() - 1) {
                out.append(" ");
            }
        }
        return out.toString();
    }

    @Override
    public void showCardDrawn(String playerName, String card) {
        if (quiet) {
            return;
        }
        System.out.println(playerName + " draws " + card);
    }

    @Override
    public void showInvalidIndexPenalty(String playerName) {
        if (quiet) {
            return;
        }
        System.out.println(playerName + " selected an invalid index and draws a penalty card.");
    }

    @Override
    public void showIllegalCardPenalty(String playerName, String card) {
        if (quiet) {
            return;
        }
        System.out.println(playerName + " tried illegal card " + card + " and draws a penalty card.");
    }

    @Override
    public void showCardPlayed(String playerName, String card) {
        if (quiet) {
            return;
        }
        System.out.println(playerName + " plays " + card);
    }

    @Override
    public void showColorCalled(String playerName, String color) {
        if (quiet) {
            return;
        }
        System.out.println(playerName + " calls " + color);
    }

    @Override
    public void showUno(String playerName) {
        if (quiet) {
            return;
        }
        System.out.println(playerName + " says UNO!");
    }

    @Override
    public void showWinnerScore(String playerName, int points) {
        if (quiet) {
            return;
        }
        System.out.println(playerName + " wins and scores " + points);
    }

    @Override
    public void showDrawTwoPenalty(String playerName) {
        if (quiet) {
            return;
        }
        System.out.println(playerName + " draws two.");
    }

    @Override
    public void showDrawFourPenalty(String playerName) {
        if (quiet) {
            return;
        }
        System.out.println(playerName + " draws four.");
    }

    @Override
    public void showSafetyLimitReached() {
        if (quiet) {
            return;
        }
        System.out.println("Game stopped at safety limit.");
    }

    @Override
    public void showChooseCardPrompt() {
        System.out.print("Choose card index/code or draw: ");
    }

    @Override
    public void showCardNotLegal() {
        System.out.println("That card is not legal.");
    }

    @Override
    public void showCardNotFound() {
        System.out.println("Card not found.");
    }

    @Override
    public void showCallColorPrompt() {
        System.out.print("Call color R/Y/G/B: ");
    }

    @Override
    public void showBadColor() {
        System.out.println("Bad color.");
    }

    @Override
    public void showPlayDrawnCardPrompt(String card) {
        System.out.print("Play drawn card " + card + "? y/n: ");
    }

    @Override
    public void showUsage() {
        System.out.println("""
                Usage:
                  java -jar target/uno-cli.jar [--bots N] [--games N] [--human] [--quiet] [--seed N]
                  java -jar target/uno-cli.jar --recent-games [N]
                  java -jar target/uno-cli.jar --player-wins "PLAYER NAME"
                  java -jar target/uno-cli.jar --highest-scores [N]
                  java -jar target/uno-cli.jar --help
                """);
    }

    @Override
    public void showInvalidPlayerCount() {
        System.out.println("UNO needs 2 to 4 players.");
    }

    @Override
    public void showUnsupportedUiType() {
        System.out.println("Unsupported UI type.");
    }

    @Override
    public void showGameHeader(int gameCount) {
        if (quiet) {
            return;
        }
        System.out.println("\n=== Game " + gameCount + " ===");
    }

    @Override
    public void showFinalScores(List<String> playerNames, int[] scores) {
        System.out.println("\nFinal scores:");
        for (int player = 0; player < playerNames.size(); player++) {
            System.out.println(playerNames.get(player) + ": " + scores[player]);
        }
    }
}
