package controller;

import model.UnoGame;
import ui.cli.CliView;

final class IntegrationCharacterizationTests {

    private final UnoGame game = new UnoGame();
    private final CliView view = new CliView();
    private final UnoGameController controller = new UnoGameController(game, view);

    int run() {
        int passed = 0;
        view.setQuiet(true);
        game.seedRandom(123);
        game.setupPlayers(3, false);
        game.clearScores();
        for (int gameCount = 0; gameCount < 5; gameCount++) {
            controller.playGame();
        }
        passed += SelfTestSupport.check(game.score(0) == 138, "seeded bot games score Bot1");
        passed += SelfTestSupport.check(game.score(1) == 246, "seeded bot games score Bot2");
        passed += SelfTestSupport.check(game.score(2) == 98, "seeded bot games score Bot3");
        view.setQuiet(false);
        return passed;
    }
}
