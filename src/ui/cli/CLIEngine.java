package ui.cli;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.function.Supplier;
import ui.PlayerInput;
import ui.Startup;
import ui.UiType;

public class CLIEngine {

    private Scanner scanner = new Scanner(System.in);
    private boolean quiet = false;

    public void setQuiet(boolean quiet) {
        this.quiet = quiet;
    }

    public Startup.Input readStartupInput(UiType uiType) {
        if (uiType instanceof UiType.Cli cli) {
            return readCliStartupInput(cli.args());
        }
        return Startup.unsupportedUi();
    }

    public Startup.Input readCliStartupInput(String[] args) {
        String bots = null;
        String games = null;
        boolean human = false;
        boolean isQuiet = false;
        String seed = null;
        Startup.Action action = Startup.Action.START_GAME;

        for (int i = 0; i < args.length; i++) {
            boolean hasNext = i + 1 < args.length;

            if (args[i].equals("--bots") && hasNext) {
                bots = args[++i];
            } else if (args[i].equals("--games") && hasNext) {
                games = args[++i];
            } else if (args[i].equals("--human")) {
                human = true;
            } else if (args[i].equals("--quiet")) {
                isQuiet = true;
            } else if (args[i].equals("--seed") && hasNext) {
                seed = args[++i];
            } else if (args[i].equals("--self-test")) {
                action = Startup.Action.SELF_TEST;
                break;
            } else if (args[i].equals("--help")) {
                action = Startup.Action.HELP;
                break;
            }
        }

        return new Startup.Input(bots, games, human, isQuiet, seed, action);
    }

    public void showTurn(String playerName, ArrayList<String> hand, String upCard, String calledColor) {
        if (quiet) {
            return;
        }
        System.out.println("\nUp card: " + upCard + (calledColor.isEmpty() ? "" : " called " + calledColor));
        System.out.println(playerName + " hand: " + join(hand));
    }

    public String join(ArrayList<String> cards) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < cards.size(); i++) {
            out.append(i).append(":").append(cards.get(i));
            if (i < cards.size() - 1) {
                out.append(" ");
            }
        }
        return out.toString();
    }

    public void showCardDrawn(String playerName, String card) {
        if (quiet) {
            return;
        }
        System.out.println(playerName + " draws " + card);
    }

    public void showInvalidIndexPenalty(String playerName) {
        if (quiet) {
            return;
        }
        System.out.println(playerName + " selected an invalid index and draws a penalty card.");
    }

    public void showIllegalCardPenalty(String playerName, String card) {
        if (quiet) {
            return;
        }
        System.out.println(playerName + " tried illegal card " + card + " and draws a penalty card.");
    }

    public void showCardPlayed(String playerName, String card) {
        if (quiet) {
            return;
        }
        System.out.println(playerName + " plays " + card);
    }

    public void showColorCalled(String playerName, String color) {
        if (quiet) {
            return;
        }
        System.out.println(playerName + " calls " + color);
    }

    public void showUno(String playerName) {
        if (quiet) {
            return;
        }
        System.out.println(playerName + " says UNO!");
    }

    public void showWinnerScore(String playerName, int points) {
        if (quiet) {
            return;
        }
        System.out.println(playerName + " wins and scores " + points);
    }

    public void showDrawTwoPenalty(String playerName) {
        if (quiet) {
            return;
        }
        System.out.println(playerName + " draws two.");
    }

    public void showDrawFourPenalty(String playerName) {
        if (quiet) {
            return;
        }
        System.out.println(playerName + " draws four.");
    }

    public void showSafetyLimitReached() {
        if (quiet) {
            return;
        }
        System.out.println("Game stopped at safety limit.");
    }

    public void showChooseCardPrompt() {
        System.out.print("Choose card index/code or draw: ");
    }

    public void showCardNotLegal() {
        System.out.println("That card is not legal.");
    }

    public void showCardNotFound() {
        System.out.println("Card not found.");
    }

    public void showCallColorPrompt() {
        System.out.print("Call color R/Y/G/B: ");
    }

    public void showBadColor() {
        System.out.println("Bad color.");
    }

    public void showPlayDrawnCardPrompt(String card) {
        System.out.print("Play drawn card " + card + "? y/n: ");
    }

    public void showUsage() {
        System.out.println("Usage: scripts/run.sh [--bots N] [--games N] [--human] [--quiet] [--seed N]");
    }

    public void showInvalidPlayerCount() {
        System.out.println("UNO needs 2 to 4 players.");
    }

    public void showUnsupportedUiType() {
        System.out.println("Unsupported UI type.");
    }

    public void showGameHeader(int gameCount) {
        if (quiet) {
            return;
        }
        System.out.println("\n=== Game " + gameCount + " ===");
    }

    public void showFinalScores(ArrayList<String> playerNames, int[] scores) {
        System.out.println("\nFinal scores:");
        for (int player = 0; player < playerNames.size(); player++) {
            System.out.println(playerNames.get(player) + ": " + scores[player]);
        }
    }

    public boolean readPlayDrawnCardDecision() {
        String input = scanner.nextLine();
        return input.equalsIgnoreCase("y") || input.equalsIgnoreCase("yes");
    }

    public PlayerInput.CardChoice readCardChoice() {
        String input = scanner.nextLine().trim().toUpperCase();
        if (input.equals("DRAW")) {
            return PlayerInput.CardChoice.draw();
        }
        try {
            return PlayerInput.CardChoice.index(Integer.parseInt(input));
        } catch (Exception ignored) {
            return PlayerInput.CardChoice.cardCode(input);
        }
    }

    public String readColorInput() {
        return scanner.nextLine().trim().toUpperCase();
    }

    public <T> T withInput(String input, Supplier<T> action) {
        Scanner originalScanner = this.scanner;
        this.scanner = new Scanner(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        try {
            return action.get();
        } finally {
            this.scanner = originalScanner;
        }
    }
}
