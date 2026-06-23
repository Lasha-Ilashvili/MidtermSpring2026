package ui.cli;

import ui.Startup;
import ui.StartupInputReader;
import ui.UiType;

final class CliStartupInputReader implements StartupInputReader {

    @Override
    public Startup.Input readStartupInput(UiType uiType) {
        if (uiType instanceof UiType.Cli cli) {
            return readCliStartupInput(cli.args());
        }
        return Startup.unsupportedUi();
    }

    Startup.Input readCliStartupInput(String[] args) {
        String bots = null;
        String games = null;
        boolean human = false;
        boolean isQuiet = false;
        String seed = null;
        String targetScore = null;
        Startup.Action action = Startup.Action.START_GAME;
        Startup.Report report = null;
        boolean gameplayOptionSeen = false;
        boolean invalidArguments = false;

        for (int i = 0; i < args.length; i++) {
            boolean hasNext = i + 1 < args.length;

            if (args[i].equals("--bots") && hasNext) {
                bots = args[++i];
                gameplayOptionSeen = true;
            } else if (args[i].equals("--games") && hasNext) {
                games = args[++i];
                gameplayOptionSeen = true;
            } else if (args[i].equals("--human")) {
                human = true;
                gameplayOptionSeen = true;
            } else if (args[i].equals("--quiet")) {
                isQuiet = true;
                gameplayOptionSeen = true;
            } else if (args[i].equals("--seed") && hasNext) {
                seed = args[++i];
                gameplayOptionSeen = true;
            } else if (args[i].equals("--target-score") && hasNext) {
                targetScore = args[++i];
                gameplayOptionSeen = true;
            } else if (args[i].equals("--recent-games")) {
                String limit = hasNext && !args[i + 1].startsWith("--") ? args[++i] : null;
                invalidArguments |= report != null;
                report = new Startup.Report(Startup.ReportType.RECENT_GAMES, limit);
            } else if (args[i].equals("--player-wins")) {
                if (hasNext && !args[i + 1].startsWith("--")) {
                    invalidArguments |= report != null;
                    report = new Startup.Report(Startup.ReportType.PLAYER_WINS, args[++i]);
                } else {
                    invalidArguments = true;
                }
            } else if (args[i].equals("--highest-scores")) {
                String limit = hasNext && !args[i + 1].startsWith("--") ? args[++i] : null;
                invalidArguments |= report != null;
                report = new Startup.Report(Startup.ReportType.HIGHEST_SCORES, limit);
            } else if (args[i].equals("--help")) {
                action = Startup.Action.HELP;
                break;
            }
        }

        if (action != Startup.Action.HELP) {
            if (invalidArguments || (report != null && gameplayOptionSeen)) {
                action = Startup.Action.INVALID_ARGUMENTS;
            } else if (report != null) {
                action = Startup.Action.SHOW_REPORT;
            }
        }

        return new Startup.Input(bots, games, human, isQuiet, seed, targetScore, action, report);
    }
}
