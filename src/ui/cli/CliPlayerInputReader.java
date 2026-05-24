package ui.cli;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.function.Supplier;
import ui.PlayerInput;
import ui.PlayerInputReader;

final class CliPlayerInputReader implements PlayerInputReader {

    private Scanner scanner = new Scanner(System.in);

    @Override
    public boolean readPlayDrawnCardDecision() {
        String input = scanner.nextLine();
        return input.equalsIgnoreCase("y") || input.equalsIgnoreCase("yes");
    }

    @Override
    public PlayerInput.CardChoice readCardChoice() {
        String input = scanner.nextLine().trim().toUpperCase();
        if (input.equals("DRAW")) {
            return PlayerInput.CardChoice.draw();
        }
        try {
            return PlayerInput.CardChoice.index(Integer.parseInt(input));
        } catch (Exception ignored) {
            return PlayerInput.CardChoice.cardCode(input);
        }
    }

    @Override
    public String readColorInput() {
        return scanner.nextLine().trim().toUpperCase();
    }

    <T> T withInput(String input, Supplier<T> action) {
        Scanner originalScanner = this.scanner;
        this.scanner = new Scanner(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        try {
            return action.get();
        } finally {
            this.scanner = originalScanner;
        }
    }
}
