import controller.Controller;
import ui.UiType;

public class Main {

    public static void main(String[] args) {
        Controller.startNewGame(new UiType.Cli(args));
    }
}
