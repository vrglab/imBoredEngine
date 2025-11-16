package org.vrglab.imBoredEngine.core.platform;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AppInfo {
    private final Map<String, String> properties = new HashMap<>();

    public AppInfo(Path infoFile) throws IOException {
        String content = Files.readString(infoFile).trim();

        Pattern pattern = Pattern.compile("(\\w+)=\\\"(.*?)\\\"");
        Matcher matcher = pattern.matcher(content);

        while (matcher.find()) {
            properties.put(matcher.group(1), matcher.group(2));
        }
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
