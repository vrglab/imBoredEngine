package org.vrglab.imBoredEngine.core.game;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vrglab.imBoredEngine.core.initializer.annotations.CalledDuringInit;
import org.vrglab.imBoredEngine.core.application.AppData;
import org.vrglab.imBoredEngine.core.debugging.CrashHandler;
import org.vrglab.imBoredEngine.core.application.AppInfo;
import org.vrglab.imBoredEngine.core.scripting.ScriptingEngine;

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
                String projectRootPath = AppData.getRuntimePath()+"/project";
                Path app_info = Path.of(projectRootPath+"/" + "App.info");

                if(hasFile(Path.of(projectRootPath), "App.info")) {
                    appInfo = new AppInfo(app_info);
                    ScriptingEngine.LoadScriptsFromDirectory(projectRootPath + "/src");
                    //TODO: Load scripts and resources
                } else {
                    throw new IllegalStateException("No Application Info file found");
                }
            }
        } catch (Throwable e) {
            CrashHandler.HandleException(e);
        }
    }
}
