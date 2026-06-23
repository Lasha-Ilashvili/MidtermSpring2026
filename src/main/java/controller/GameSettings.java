package controller;

import ui.Startup;

record GameSettings(int bots, int games, long seed, boolean human, Integer targetScore) {

    private static final int DEFAULT_TARGET_ROUND_CAP = 100;

    static GameSettings from(Startup.Input startupInput) {
        int bots = 3;
        int games = startupInput.targetScore() == null ? 1 : DEFAULT_TARGET_ROUND_CAP;
        long seed = System.currentTimeMillis();
        Integer targetScore = null;

        if (startupInput.bots() != null) {
            bots = Integer.parseInt(startupInput.bots());
        }
        if (startupInput.games() != null) {
            games = Integer.parseInt(startupInput.games());
        }
        if (startupInput.seed() != null) {
            seed = Long.parseLong(startupInput.seed());
        }
        if (startupInput.targetScore() != null) {
            targetScore = Integer.parseInt(startupInput.targetScore());
        }

        return new GameSettings(bots, games, seed, startupInput.human(), targetScore);
    }

    boolean hasTargetScore() {
        return targetScore != null;
    }
}
