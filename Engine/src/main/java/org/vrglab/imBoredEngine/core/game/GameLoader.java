package org.vrglab.imBoredEngine.core.game;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vrglab.imBoredEngine.core.initializer.annotations.CalledDuringInit;
import org.vrglab.imBoredEngine.core.application.AppData;
import org.vrglab.imBoredEngine.core.debugging.CrashHandler;
import org.vrglab.imBoredEngine.core.application.AppInfo;
import org.vrglab.imBoredEngine.core.resourceManagment.ResourceEntry;
import org.vrglab.imBoredEngine.core.resourceManagment.ResourceManager;
import org.vrglab.imBoredEngine.core.scripting.ScriptingEngine;
import org.vrglab.imBoredEngine.tools.vibeloader.VibeLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.vrglab.imBoredEngine.core.utils.IoUtils.getFirstVibeFile;
import static org.vrglab.imBoredEngine.core.utils.IoUtils.hasFile;

public class GameLoader {
    private static Logger LOGGER = LogManager.getLogger(GameLoader.class);

    private static AppInfo appInfo;

    private static List<ResourceEntry> vibeScripts = new ArrayList<>();
    private static List<ResourceEntry> resources = new ArrayList<>();

    private static Path appInfoPath = null;


    public static AppInfo getAppInfo() {
        return appInfo;
    }

    @CalledDuringInit(priority = 2)
    public static void Init() {
        LOGGER.info("Loading Game Info");

        try {
            if(!AppData.isTool()) {
                if(hasFile(Path.of(AppData.getRuntimePath()), "*.vibe")) {
                    VibeLoader.loadVibe(getFirstVibeFile(Path.of(AppData.getRuntimePath())));
                    VibeLoader.listEntries().forEach(resourceEntry -> {
                        byte[] rawData = VibeLoader.getDecryptedResource(resourceEntry.getName());
                        resourceEntry.setRawData(rawData);

                        if(resourceEntry.getName().equals("App.info")) {
                            try {
                                appInfoPath = Files.createFile(Path.of(AppData.getRuntimePath()+"/App.info"));
                                Files.write(appInfoPath, rawData);
                                appInfo = new AppInfo(appInfoPath);
                            } catch (IOException e) {
                                CrashHandler.HandleException(e);
                            }
                        } else if(resourceEntry.getName().contains(".lua")) {
                            ScriptingEngine.loadResourceEntry(resourceEntry);
                        }
                        else {
                            ResourceManager.loadResourceFromEntry(resourceEntry);
                        }
                    });
                } else {
                    LOGGER.warn("We are in Editor Environment, Loading in Editor mode");
                    String projectRootPath = AppData.getRuntimePath()+"/project";
                    appInfoPath = Path.of(projectRootPath+"/" + "App.info");

                    if(hasFile(Path.of(projectRootPath), "App.info")) {
                        appInfo = new AppInfo(appInfoPath);
                        ScriptingEngine.loadScriptsFromDirectory(projectRootPath + "/src");
                        ResourceManager.loadResourcesFromDirectory(projectRootPath + "/assets");
                    } else {
                        throw new IllegalStateException("No Application Info file found");
                    }
                }
            } else {
                appInfo = new AppInfo();
            }
        } catch (Throwable e) {
            CrashHandler.HandleException(e);
        }
    }
}
