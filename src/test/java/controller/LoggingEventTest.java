package controller;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import game.UnoGame;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import ui.UiType;
import ui.cli.CliView;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class LoggingEventTest {

    @Test
    void logsRequiredGameEvents() {
        Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        Level originalLevel = root.getLevel();
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        root.setLevel(Level.INFO);
        root.addAppender(appender);

        try {
            OutputCapture output = new OutputCapture();
            output.capture(() -> {
                UnoGameController.startNewGame(new UiType.Cli(new String[]{
                        "--bots", "3",
                        "--games", "1",
                        "--quiet",
                        "--seed", "123"
                }));
                return null;
            });

            UnoGame game = new UnoGame();
            CliView view = new CliView();
            UnoGameController controller = new UnoGameController(game, view);
            output.capture(() -> {
                game.setupCurrentHumanTurn("R5", List.of("B3", "R9"));
                return view.withInput("B3\nR9\n", controller::askHuman);
            });

            List<String> messages = appender.list.stream()
                    .map(ILoggingEvent::getFormattedMessage)
                    .toList();

            assertAll(
                    () -> assertEvent(messages, "event=game_start"),
                    () -> assertEvent(messages, "event=player_turn"),
                    () -> assertEvent(messages, "event=card_played"),
                    () -> assertEvent(messages, "event=card_drawn"),
                    () -> assertEvent(messages, "event=invalid_input"),
                    () -> assertEvent(messages, "event=round_end"),
                    () -> assertEvent(messages, "event=session_end")
            );
        } finally {
            root.detachAppender(appender);
            root.setLevel(originalLevel);
            appender.stop();
        }
    }

    private static void assertEvent(List<String> messages, String event) {
        assertTrue(messages.stream().anyMatch(message -> message.startsWith(event)),
                () -> "Missing log event: " + event);
    }
}
