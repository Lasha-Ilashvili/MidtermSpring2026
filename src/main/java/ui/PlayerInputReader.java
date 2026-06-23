package ui;

public interface PlayerInputReader {

    boolean readPlayDrawnCardDecision();

    default PlayerInput.DrawnCardDecision readPlayDrawnCardChoice() {
        return PlayerInput.DrawnCardDecision.from(readPlayDrawnCardDecision());
    }

    PlayerInput.CardChoice readCardChoice();

    String readColorInput();
}
