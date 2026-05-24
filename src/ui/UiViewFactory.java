package ui;

import ui.cli.CLIEngine;

public final class UiViewFactory {

    private UiViewFactory() {
    }

    public static UiView create(UiType uiType) {
        return switch (uiType) {
            case UiType.Cli cli -> new CLIEngine();
        };
    }
}
