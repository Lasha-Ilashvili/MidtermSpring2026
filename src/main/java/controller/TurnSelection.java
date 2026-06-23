package controller;

record TurnSelection(int index, boolean unoCalled) {

    static TurnSelection draw() {
        return new TurnSelection(-1, false);
    }
}
