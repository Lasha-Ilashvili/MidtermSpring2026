package ui.cli;

import java.util.Scanner;
import model.Model;
import ui.UIEngine;
import ui.UiEvent;

public class CLIEngine implements UIEngine {

    private final Scanner scanner;

    public CLIEngine() {
        this(new Scanner(System.in));
    }

    CLIEngine(Scanner scanner) {
        this.scanner = scanner;
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
