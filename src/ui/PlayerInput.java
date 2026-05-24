package ui;

public final class PlayerInput {

    private PlayerInput() {
    }

    public enum CardChoiceType {
        DRAW,
        INDEX,
        CARD_CODE
    }

    public record CardChoice(CardChoiceType type, int index, String cardCode) {

        public static CardChoice draw() {
            return new CardChoice(CardChoiceType.DRAW, -1, "");
        }

        public static CardChoice index(int index) {
            return new CardChoice(CardChoiceType.INDEX, index, "");
        }

        public static CardChoice cardCode(String cardCode) {
            return new CardChoice(CardChoiceType.CARD_CODE, -1, cardCode);
        }
    }
}
