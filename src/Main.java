import controller.UnoGameController;
import ui.UiType;

public class Main {

    public static void main(String[] args) {
        UnoGameController.startNewGame(new UiType.Cli(args));
    }
}
