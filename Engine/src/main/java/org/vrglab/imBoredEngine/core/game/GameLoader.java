package org.vrglab.imBoredEngine.core.game;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vrglab.imBoredEngine.core.initializer.interfaces.CalledDuringInit;
import org.vrglab.imBoredEngine.core.platform.AppData;
import org.vrglab.imBoredEngine.core.debugging.CrashHandler;
import org.vrglab.imBoredEngine.core.platform.AppInfo;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.vrglab.imBoredEngine.core.utils.IoUtils.hasFile;

public class GameLoader {
    private static Logger LOGGER = LogManager.getLogger(GameLoader.class);

    private static AppInfo appInfo;


    public static AppInfo getAppInfo() {
        return appInfo;
    }

    @CalledDuringInit(priority = 2)
    private static void Init() {
        LOGGER.info("Loading Game Info");

        try {
            if(hasFile(Path.of(AppData.getRuntimePath()), "*.vibe")) {

            } else {
                LOGGER.warn("We are in Editor Environment, Loading in Editor mode");
                Path app_info = Path.of(AppData.getRuntimePath()+"/project/" + "App.info");
                if(hasFile(Path.of(AppData.getRuntimePath()+"/project"), "App.info")) {
                    appInfo = new AppInfo(app_info);

                } else {
                    throw new IllegalStateException("No Application Info file found");
                }
            }
        } catch (Throwable e) {
            CrashHandler.HandleException(e);
        }
    }
}
