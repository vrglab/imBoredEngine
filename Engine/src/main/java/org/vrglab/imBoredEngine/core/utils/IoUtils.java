package org.vrglab.imBoredEngine.core.utils;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

public final class IoUtils {
    public static boolean hasFile(Path folder, String file) throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(folder, file)) {
            return stream.iterator().hasNext();
        }
    }
}
