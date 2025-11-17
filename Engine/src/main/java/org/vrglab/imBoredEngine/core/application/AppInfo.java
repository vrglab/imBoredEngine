package org.vrglab.imBoredEngine.core.application;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vrglab.imBoredEngine.core.debugging.CrashHandler;
import org.vrglab.imBoredEngine.core.game.GameLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AppInfo {
    private final Map<String, String> properties = new HashMap<>();

    private static Logger LOGGER = LogManager.getLogger(AppInfo.class);

    public AppInfo(Path infoFile) {

        AtomicReference<String> content =  new AtomicReference<>();

        Threading.io().execute(() -> {
            try {
                LOGGER.info("Loading app info from file {}", infoFile.toAbsolutePath().toString());
                content.set(Files.readString(infoFile).trim());
            } catch (IOException e) {
                CrashHandler.HandleException(e);
            }

            Pattern pattern = Pattern.compile("(\\w+)=\\\"(.*?)\\\"");
            Matcher matcher = pattern.matcher(content.get());

            while (matcher.find()) {
                properties.put(matcher.group(1), matcher.group(2));
            }
        });
    }

    public String get(String key) {
        return properties.get(key);
    }

    public String getName() {
        return get("name");
    }

    public String getAuthor() {
        return get("author");
    }

    public String getVersion() {
        return get("version");
    }

    public String getEntry() {
        return get("entry");
    }

    public String getEngineVersion() {
        return get("engine_version");
    }
}
