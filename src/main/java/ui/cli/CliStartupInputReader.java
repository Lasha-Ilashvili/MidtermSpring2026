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
            } else if (args[i].equals("--help")) {
                action = Startup.Action.HELP;
                break;
            }
        }

        return new Startup.Input(bots, games, human, isQuiet, seed, action);
    }
}
