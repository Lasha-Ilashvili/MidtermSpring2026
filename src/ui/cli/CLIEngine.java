package ui.cli;

import java.util.Objects;
import java.util.Scanner;
import model.Model;
import ui.UIEngine;
import ui.UiEvent;

public class CLIEngine implements UIEngine {

    private final String[] args;
    private final Scanner scanner;

    public CLIEngine(String[] args) {
        this(args, new Scanner(System.in));
    }

    CLIEngine(String[] args, Scanner scanner) {
        this.args = Objects.requireNonNull(args).clone();
        this.scanner = Objects.requireNonNull(scanner);
    }

    @Override
    public void onInit() {

    }

    @Override
    public void onDraw(Model model) {

    }

    @Override
    public UiEvent readEvent() {
        return new UiEvent.TextInput(scanner.nextLine());
    }

    @Override
    public void onDestroy() {

    }
}
