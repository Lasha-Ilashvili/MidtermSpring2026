package model;

enum CardColor {
    RED("R"),
    YELLOW("Y"),
    GREEN("G"),
    BLUE("B"),
    NONE("");

    private final String code;

    CardColor(String code) {
        this.code = code;
    }

    String code() {
        return code;
    }

    boolean matchesCall(String calledColor) {
        return !calledColor.isEmpty() && code.equals(calledColor);
    }

    static boolean isPlayableCode(String code) {
        return RED.code.equals(code)
                || YELLOW.code.equals(code)
                || GREEN.code.equals(code)
                || BLUE.code.equals(code);
    }

    static CardColor fromCardCode(String cardCode) {
        if (cardCode.startsWith(RED.code)) {
            return RED;
        }
        if (cardCode.startsWith(YELLOW.code)) {
            return YELLOW;
        }
        if (cardCode.startsWith(GREEN.code)) {
            return GREEN;
        }
        if (cardCode.startsWith(BLUE.code)) {
            return BLUE;
        }
        return NONE;
    }
}
