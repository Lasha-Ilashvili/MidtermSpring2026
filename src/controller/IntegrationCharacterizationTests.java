package controller;

import ui.UiType;

final class IntegrationCharacterizationTests {

    private final OutputCapture output = new OutputCapture();

    int run() {
        int passed = 0;
        output.capture(() -> {
            UnoGameController.startNewGame(new UiType.Cli(new String[]{
                    "--bots", "3",
                    "--games", "5",
                    "--quiet",
                    "--seed", "123"
            }));
            return null;
        });
        passed += SelfTestSupport.check(output.contains("Bot1: 138"), "seeded bot games score Bot1");
        passed += SelfTestSupport.check(output.contains("Bot2: 246"), "seeded bot games score Bot2");
        passed += SelfTestSupport.check(output.contains("Bot3: 98"), "seeded bot games score Bot3");
        return passed;
    }
}
