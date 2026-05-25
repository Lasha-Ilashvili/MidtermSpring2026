package game;

import java.util.ArrayList;
import java.util.Arrays;

final class Players {

    private final ArrayList<ArrayList<String>> hands = new ArrayList<>();
    private final ArrayList<String> playerNames = new ArrayList<>();
    private final ArrayList<Boolean> humanPlayers = new ArrayList<>();
    private final int[] scores = new int[10];

    void setup(int bots, boolean human) {
        playerNames.clear();
        humanPlayers.clear();
        hands.clear();

        if (human) {
            playerNames.add("You");
            humanPlayers.add(Boolean.TRUE);
            hands.add(new ArrayList<>());
        }

        for (int i = 1; i <= bots; i++) {
            playerNames.add("Bot" + i);
            humanPlayers.add(Boolean.FALSE);
            hands.add(new ArrayList<>());
        }
    }

    ArrayList<String> namesSnapshot() {
        return new ArrayList<>(playerNames);
    }

    int[] scoresSnapshot() {
        return scores.clone();
    }

    void clearScores() {
        Arrays.fill(scores, 0);
    }

    int score(int player) {
        return scores[player];
    }

    void addScore(int player, int points) {
        scores[player] += points;
    }

    int scoreFromOpponents(int winner) {
        int points = 0;
        for (int i = 0; i < count(); i++) {
            if (i != winner) {
                for (int j = 0; j < hand(i).size(); j++) {
                    points += CardRules.points(hand(i).get(j));
                }
            }
        }
        addScore(winner, points);
        return points;
    }

    int count() {
        return playerNames.size();
    }

    String name(int player) {
        return playerNames.get(player);
    }

    boolean isHuman(int player) {
        return humanPlayers.get(player);
    }

    ArrayList<String> hand(int player) {
        return hands.get(player);
    }

    void clearHands() {
        for (ArrayList<String> hand : hands) {
            hand.clear();
        }
    }
}
