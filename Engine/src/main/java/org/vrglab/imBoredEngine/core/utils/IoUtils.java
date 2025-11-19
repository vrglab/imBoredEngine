package org.vrglab.imBoredEngine.core.utils;

import org.vrglab.imBoredEngine.core.debugging.CrashHandler;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class IoUtils {
    public static boolean hasFile(Path folder, String file) throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(folder, file)) {
            return stream.iterator().hasNext();
        }
    }

    public static Path getFirstVibeFile(Path folder) {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(folder, "*.vibe")) {
            for (Path file : stream) {
                return file; // return first match
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null; // no file found
    }

    public static List<Path> getFiles(String path) {
        try {
          return Files.walk(Paths.get(path))
                    .filter(Files::isRegularFile)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            CrashHandler.HandleException(e);
            return null;
        }
    }

    public static String streamToString(InputStream stream){
        byte[] stringBytes = null;
        try {
            stringBytes = stream.readAllBytes();
        } catch (IOException e) {
            CrashHandler.HandleException(e);
        }
        return new String(stringBytes);
    }

}
