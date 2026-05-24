package model;

final class CardRules {

    private CardRules() {
    }

    static boolean isPlayableColor(String color) {
        return color.equals("R") || color.equals("Y") || color.equals("G") || color.equals("B");
    }

    static boolean isLegal(String card, String up, String call) {
        if (card.startsWith("W")) {
            return true;
        }
        if (color(card).equals(color(up))) {
            return true;
        }
        if (!call.isEmpty() && color(card).equals(call)) {
            return true;
        }
        if (rank(card).equals(rank(up)) && !rank(card).equals("NUMBER")) {
            return true;
        }
        return rank(card).equals("NUMBER") && rank(up).equals("NUMBER") && number(card) == number(up);
    }

    static boolean isWildCard(String card) {
        return card.equals("W") || card.equals("W4");
    }

    static String color(String card) {
        if (card.startsWith("R")) {
            return "R";
        }
        if (card.startsWith("Y")) {
            return "Y";
        }
        if (card.startsWith("G")) {
            return "G";
        }
        if (card.startsWith("B")) {
            return "B";
        }
        return "";
    }

    static String rank(String card) {
        if (card.equals("W")) {
            return "WILD";
        }
        if (card.equals("W4")) {
            return "WILD_DRAW_FOUR";
        }
        if (card.endsWith("S")) {
            return "SKIP";
        }
        if (card.endsWith("R")) {
            return "REVERSE";
        }
        if (card.endsWith("+2")) {
            return "DRAW_TWO";
        }
        return "NUMBER";
    }

    static int number(String card) {
        if (rank(card).equals("NUMBER")) {
            return Integer.parseInt(card.substring(1));
        }
        return -1;
    }

    static int points(String card) {
        String r = rank(card);
        return switch (r) {
            case "NUMBER" -> number(card);
            case "SKIP", "REVERSE", "DRAW_TWO" -> 20;
            case "WILD", "WILD_DRAW_FOUR" -> 50;
            default -> 0;
        };
    }
}
