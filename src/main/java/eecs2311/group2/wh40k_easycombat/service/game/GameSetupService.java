package eecs2311.group2.wh40k_easycombat.service.game;

import eecs2311.group2.wh40k_easycombat.model.instance.GameSetupConfig;

public class GameSetupService {
    private static final GameSetupService INSTANCE = new GameSetupService();

    private GameSetupConfig currentConfig;

    public static GameSetupService getInstance() {
        return INSTANCE;
    }

    public synchronized void setCurrentConfig(GameSetupConfig currentConfig) {
        this.currentConfig = currentConfig;
    }

    public synchronized GameSetupConfig getCurrentConfig() {
        return currentConfig;
    }

    public synchronized void clear() {
        currentConfig = null;
    }
}
