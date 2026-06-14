package game;

final class PlayArea {

    private String upCard = "";
    private String calledColor = "";

    String upCard() {
        return upCard;
    }

    void setUpCard(String upCard) {
        this.upCard = upCard;
    }

    String calledColor() {
        return calledColor;
    }

    void setCalledColor(String calledColor) {
        this.calledColor = calledColor;
    }

    void clearCalledColor() {
        calledColor = "";
    }
}
