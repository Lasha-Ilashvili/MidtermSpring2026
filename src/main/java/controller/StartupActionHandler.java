package controller;

import ui.Startup;
import ui.UiView;

final class StartupActionHandler {

    private final UiView view;

    StartupActionHandler(UiView view) {
        this.view = view;
    }

    boolean handle(Startup.Action action) {
        return switch (action) {
            case HELP -> {
                view.showUsage();
                yield true;
            }
            case UNSUPPORTED_UI -> {
                view.showUnsupportedUiType();
                yield true;
            }
            case START_GAME -> false;
        };
    }
}
