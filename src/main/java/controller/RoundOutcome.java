package controller;

import history.CompletedRound;

record RoundOutcome(CompletedRound.Status status, String winnerName, int pointsAwarded) {

    static RoundOutcome completed(String winnerName, int pointsAwarded) {
        return new RoundOutcome(CompletedRound.Status.COMPLETED, winnerName, pointsAwarded);
    }

    static RoundOutcome safetyLimit() {
        return new RoundOutcome(CompletedRound.Status.SAFETY_LIMIT, null, 0);
    }
}
