package controller;

import org.junit.jupiter.api.Test;
import ui.UiType;
import ui.cli.CliView;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class TargetScoreControllerTest {

    @Test
    void targetScoreDefaultsToRoundCapWhenGamesIsOmitted() {
        CliView view = new CliView();

        GameSettings settings = GameSettings.from(view.readCliStartupInput(new String[]{
                "--bots", "3",
                "--target-score", "500",
                "--seed", "123"
        }));

        assertEquals(3, settings.bots());
        assertEquals(100, settings.games());
        assertEquals(500, settings.targetScore());
        assertTrue(settings.hasTargetScore());
    }

    @Test
    void gamesRemainExactRoundCountWithoutTargetScore() {
        CliView view = new CliView();

        GameSettings settings = GameSettings.from(view.readCliStartupInput(new String[]{
                "--bots", "3",
                "--games", "5",
                "--seed", "123"
        }));

        assertEquals(5, settings.games());
        assertFalse(settings.hasTargetScore());
    }

    @Test
    void targetScoreStopsSessionBeforeRoundCap() {
        OutputCapture output = new OutputCapture();

        output.capture(() -> {
            UnoGameController.startNewGame(new UiType.Cli(new String[]{
                    "--bots", "3",
                    "--games", "5",
                    "--target-score", "1",
                    "--seed", "123"
            }));
            return null;
        });

        assertTrue(output.contains("=== Game 1 ==="));
        assertFalse(output.contains("=== Game 2 ==="));
    }
}
