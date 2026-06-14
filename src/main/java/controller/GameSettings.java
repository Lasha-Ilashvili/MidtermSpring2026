package controller;

import ui.Startup;

record GameSettings(int bots, int games, long seed, boolean human) {

    static GameSettings from(Startup.Input startupInput) {
        int bots = 3;
        int games = 1;
        long seed = System.currentTimeMillis();

        if (startupInput.bots() != null) {
            bots = Integer.parseInt(startupInput.bots());
        }
        if (startupInput.games() != null) {
            games = Integer.parseInt(startupInput.games());
        }
        if (startupInput.seed() != null) {
            seed = Long.parseLong(startupInput.seed());
        }

        return new GameSettings(bots, games, seed, startupInput.human());
    }
}
