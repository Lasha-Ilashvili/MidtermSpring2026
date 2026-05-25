package model;

final class CardRules {

    private CardRules() {
    }

    static boolean isPlayableColor(String color) {
        return CardColor.isPlayableCode(color);
    }

    static boolean isLegal(String card, String up, String call) {
        Card candidate = Card.fromCode(card);
        Card upCard = Card.fromCode(up);

        if (candidate.isWild()) {
            return true;
        }
        if (candidate.color() == upCard.color()) {
            return true;
        }
        if (candidate.color().matchesCall(call)) {
            return true;
        }
        if (candidate.canMatchRank(upCard)) {
            return true;
        }
        return candidate.canMatchNumber(upCard);
    }

    static boolean isWildCard(String card) {
        return Card.fromCode(card).isWild();
    }

    static String color(String card) {
        return Card.fromCode(card).colorCode();
    }

    static String rank(String card) {
        return Card.fromCode(card).rank().name();
    }

    static int number(String card) {
        return Card.fromCode(card).number();
    }

    static int points(String card) {
        return Card.fromCode(card).points();
    }
}
