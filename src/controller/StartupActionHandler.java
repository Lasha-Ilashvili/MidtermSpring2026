package controller;

import ui.Startup;
import ui.UiView;

final class StartupActionHandler {

    private final UiView view;

    StartupActionHandler(UiView view) {
        this.view = view;
    }

    boolean handle(Startup.Action action) {
        if (action == Startup.Action.SELF_TEST) {
            CharacterizationTests.run();
            return true;
        } else if (action == Startup.Action.HELP) {
            view.showUsage();
            return true;
        } else if (action == Startup.Action.UNSUPPORTED_UI) {
            view.showUnsupportedUiType();
            return true;
        }
        return false;
    }
}
