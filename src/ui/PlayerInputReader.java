package ui;

public interface PlayerInputReader {

    boolean readPlayDrawnCardDecision();

    PlayerInput.CardChoice readCardChoice();

    String readColorInput();
}
