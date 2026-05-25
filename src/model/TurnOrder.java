package model;

final class TurnOrder {

    private int currentPlayer = 0;
    private int direction = 1;

    int currentPlayer() {
        return currentPlayer;
    }

    int direction() {
        return direction;
    }

    void setCurrentPlayer(int currentPlayer) {
        this.currentPlayer = currentPlayer;
    }

    void setDirection(int direction) {
        this.direction = direction;
    }

    void reset() {
        direction = 1;
    }

    void reverseDirection() {
        direction = direction * -1;
    }

    void next(int playerCount) {
        currentPlayer += direction;
        if (currentPlayer >= playerCount) {
            currentPlayer = 0;
        }
        if (currentPlayer < 0) {
            currentPlayer = playerCount - 1;
        }
    }
}
