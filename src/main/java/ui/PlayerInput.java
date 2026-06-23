package ui;

public final class PlayerInput {

    private PlayerInput() {
    }

    public enum CardChoiceType {
        DRAW,
        INDEX,
        CARD_CODE
    }

    public record CardChoice(CardChoiceType type, int index, String cardCode, boolean unoCalled) {

        public static CardChoice draw() {
            return new CardChoice(CardChoiceType.DRAW, -1, "", false);
        }

        public static CardChoice index(int index) {
            return index(index, false);
        }

        public static CardChoice index(int index, boolean unoCalled) {
            return new CardChoice(CardChoiceType.INDEX, index, "", unoCalled);
        }

        public static CardChoice cardCode(String cardCode) {
            return cardCode(cardCode, false);
        }

        public static CardChoice cardCode(String cardCode, boolean unoCalled) {
            return new CardChoice(CardChoiceType.CARD_CODE, -1, cardCode, unoCalled);
        }
    }

    public record DrawnCardDecision(boolean play, boolean unoCalled) {

        public static DrawnCardDecision from(boolean play) {
            return new DrawnCardDecision(play, false);
        }
    }
}
