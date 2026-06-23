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
        return readPlayDrawnCardChoice().play();
    }

    @Override
    public PlayerInput.DrawnCardDecision readPlayDrawnCardChoice() {
        ParsedUnoInput input = parseUnoSuffix(scanner.nextLine());
        boolean play = input.value().equalsIgnoreCase("y") || input.value().equalsIgnoreCase("yes");
        return new PlayerInput.DrawnCardDecision(play, input.unoCalled());
    }

    @Override
    public PlayerInput.CardChoice readCardChoice() {
        ParsedUnoInput parsed = parseUnoSuffix(scanner.nextLine());
        String input = parsed.value().toUpperCase();
        if (input.equals("DRAW")) {
            return PlayerInput.CardChoice.draw();
        }
        try {
            return PlayerInput.CardChoice.index(Integer.parseInt(input), parsed.unoCalled());
        } catch (Exception ignored) {
            return PlayerInput.CardChoice.cardCode(input, parsed.unoCalled());
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

    private ParsedUnoInput parseUnoSuffix(String input) {
        String trimmed = input.trim();
        String[] parts = trimmed.split("\\s+");
        if (parts.length > 1 && parts[parts.length - 1].equalsIgnoreCase("UNO")) {
            return new ParsedUnoInput(parts[0], true);
        }
        return new ParsedUnoInput(trimmed, false);
    }

    private record ParsedUnoInput(String value, boolean unoCalled) {
    }
}
