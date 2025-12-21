package org.vrglab.imBoredEngine.core.levels;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vrglab.imBoredEngine.core.graphics.rendering.BgfxManager;
import org.vrglab.imBoredEngine.core.initializer.annotations.CalledDuringInit;
import org.vrglab.imBoredEngine.core.levels.world.Level;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LevelManager {

    private static final Logger LOGGER = LogManager.getLogger(LevelManager.class);

    private static Map<String, Level> levels = new HashMap();

    private static Map<String, Level> levelInstances = new HashMap();

    private static Level currentLevel = null;

    private LevelManager() {}

    public static void addLevel(String name, Level level) {
        levels.put(name, level);
    }

    public static Level getLevel(String name) {
        return levels.get(name);
    }


    @CalledDuringInit(priority = 8)
    private static void init() {
        LOGGER.info("Starting Level Manager");
    }

}
