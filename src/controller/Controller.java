package controller;

import model.Model;
import ui.UIEngine;
import ui.UiEvent;

public class Controller {

    private final Model model;
    private final UIEngine uiEngine;

    public static void startNewGame(UIEngine uiEngine) {
        new Controller(uiEngine, new Model()).startGame();
    }

    private Controller(UIEngine uiEngine, Model model) {
        this.uiEngine = uiEngine;
        this.model = model;
    }

    private void startGame() {
        uiEngine.onInit();

        while (true) {
            uiEngine.onDraw(model);

            // TODO: If game ends break

            UiEvent event = uiEngine.readEvent();
        }

        uiEngine.onDestroy();
    }
}
