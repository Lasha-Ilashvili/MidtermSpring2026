package controller;

import model.Model;
import ui.UIEngine;
import ui.UiEvent;
import ui.UiType;
import ui.cli.CLIEngine;

public class Controller {

    private final Model model;
    private final UIEngine uiEngine;

    public static void startNewGame(UiType uiType) {
        UIEngine uiEngine = switch (uiType) {
            case UiType.Cli cli -> new CLIEngine(cli.args());
        };

        new Controller(uiEngine, new Model()).startGame();
    }

    private Controller(UIEngine uiEngine, Model model) {
        this.uiEngine = uiEngine;
        this.model = model;
    }

    private void startGame() {
        uiEngine.onInit();

        boolean isRunning = true;
        while (isRunning) {
            uiEngine.onDraw(model);

            // TODO: If game ends break

            UiEvent event = uiEngine.readEvent();
        }

        uiEngine.onDestroy();
    }
}
