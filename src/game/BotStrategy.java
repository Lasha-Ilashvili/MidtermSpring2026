package game;

import java.util.List;

final class BotStrategy {

    private static final List<CardRank> PLAY_PRIORITY = List.of(
            CardRank.DRAW_TWO,
            CardRank.SKIP,
            CardRank.NUMBER
    );

    private BotStrategy() {
    }

    static int chooseBotCard(List<String> hand, String upCard, String calledColor) {
        for (CardRank rank : PLAY_PRIORITY) {
            int chosen = chooseFirstLegalCardByRank(hand, rank, upCard, calledColor);
            if (chosen != -1) {
                return chosen;
            }
        }
        for (int i = 0; i < hand.size(); i++) {
            if (Card.fromCode(hand.get(i)).hasWildPrefix()) {
                return i;
            }
        }
        return -1;
    }

    static int chooseFirstLegalCardByRank(List<String> hand, CardRank targetRank, String upCard, String calledColor) {
        for (int i = 0; i < hand.size(); i++) {
            String card = hand.get(i);
            if (CardRules.rankValue(card) == targetRank && CardRules.isLegal(card, upCard, calledColor)) {
                return i;
            }
        }
        return -1;
    }

    static String chooseBotColor(List<String> hand) {
        int r = 0;
        int y = 0;
        int g = 0;
        int b = 0;
        for (String s : hand) {
            String c = CardRules.color(s);
            switch (c) {
                case "R" -> r++;
                case "Y" -> y++;
                case "G" -> g++;
                case "B" -> b++;
            }
        }
        if (r >= y && r >= g && r >= b) {
            return "R";
        } else if (y >= r && y >= g && y >= b) {
            return "Y";
        } else if (g >= r && g >= y && g >= b) {
            return "G";
        } else {
            return "B";
        }
    }
}
