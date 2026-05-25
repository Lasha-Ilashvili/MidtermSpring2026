package controller;

import ui.cli.CliView;

final class CliCharacterizationTests {

    private final CliView view = new CliView();

    int run() {
        int passed = 0;
        passed += SelfTestSupport.check(
                view.join(SelfTestSupport.cards("R5", "W", "B+2")).equals("0:R5 1:W 2:B+2"),
                "hand display includes indexes");
        return passed;
    }
}
