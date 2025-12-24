package org.vrglab.imBoredEngine.core.resourceManagment;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vrglab.imBoredEngine.core.application.Threading;
import org.vrglab.imBoredEngine.core.debugging.CrashHandler;
import org.vrglab.imBoredEngine.core.graphics.rendering.annotations.CalledAfterBGFXInit;
import org.vrglab.imBoredEngine.core.initializer.annotations.CalledDuringInit;
import org.vrglab.imBoredEngine.core.resourceManagment.Annotations.ResourceLoaders;
import org.vrglab.imBoredEngine.core.resourceManagment.interfaces.ResourceLoader;
import org.vrglab.imBoredEngine.core.utils.IoUtils;
import org.vrglab.imBoredEngine.core.utils.ReflectionsUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

public class ResourceManager {

    private static final Logger LOGGER = LogManager.getLogger(ResourceManager.class);

    private static Map<String, ResourceLoader> resourceLoaders = new HashMap();
    private static Cache<String, Resource> resourcesCache = CacheBuilder.newBuilder().build();

    private static Map<Path, File> toBeLoadedResources = new HashMap<>();
    private static Map<Path, ResourceEntry> toBeLoadedEntries = new HashMap<>();

    private final static ResourceLoader<String> DEFAULT_LOADER = new ResourceLoader() {
        @Override
        public String getResourceRegex() {
            return "*.*";
        }

        @Override
        public String load(byte[] fileContent) {
            return new String(fileContent);
        }
    };


    @CalledDuringInit(priority = 7)
    private static void init() {
        LOGGER.info("Starting Resource Manager");
       Set<Class<?>> resourcesLoadersFoundUninitialized = ReflectionsUtil.findClasses(ResourceLoaders.class);

        resourcesLoadersFoundUninitialized.forEach(resourceLoadersFoundUninitialized -> {
            ResourceLoader loader = (ResourceLoader)ReflectionsUtil.createInstance(resourceLoadersFoundUninitialized);
            resourceLoaders.put(loader.getResourceRegex(), loader);
        });
    }

    @CalledAfterBGFXInit
    private static void loadCachedResources() {
        LOGGER.info("Loading cached Resources to Engine");
        toBeLoadedResources.forEach((path, file) -> {
            loadResource(file.getAbsolutePath());
        });

        toBeLoadedEntries.forEach((path, file) -> {
            loadResource(file);
        });
    }

    public static <T> Resource<T> loadResource(String file) {
        if(resourcesCache.asMap().containsKey(file)) {
            return resourcesCache.getIfPresent(file);
        }
        File f = new File(file);
        ByteArrayOutputStream  outputStream = new ByteArrayOutputStream();
        try {
            outputStream.write(Files.readAllBytes(f.toPath()));
        } catch (IOException e) {
            CrashHandler.HandleException(e);
        }
        ResourceLoader<T> loader = getFileResourceLoader(f.getAbsolutePath());
        T loaded = loader.load(outputStream.toByteArray());
        Resource<T> constructedResource = new Resource<T>(outputStream.toByteArray(),loaded, f, f.getName());
        resourcesCache.put(file, constructedResource);
        return constructedResource;
    }

    public static <T> Resource<T> loadResource(ResourceEntry entry) {
        Path name = Path.of(entry.getName()).getFileName();
        if(resourcesCache.asMap().containsKey(String.valueOf(name))) {
            return resourcesCache.getIfPresent(String.valueOf(name));
        }
        ByteArrayOutputStream  outputStream = new ByteArrayOutputStream();
        try {
            outputStream.write(entry.getRawData());
        } catch (IOException e) {
            CrashHandler.HandleException(e);
        }
        ResourceLoader<T> loader = getFileResourceLoader(String.valueOf(name));
        T loaded = loader.load(outputStream.toByteArray());
        Resource<T> constructedResource = new Resource<T>(outputStream.toByteArray(),loaded, null, String.valueOf(name));
        resourcesCache.put(String.valueOf(name), constructedResource);
        return constructedResource;
    }

    public static <T> T getResource(Class<T> type, String file) {
        AtomicReference<Resource<T>> resource = new AtomicReference<>(resourcesCache.getIfPresent(file));
        if(resource.get() == null) {
            resourcesCache.asMap().forEach((key, value) -> {
                if(value.getResourceName().equals(file)) {
                    resource.set((Resource<T>) value);
                }
            });
        }
        return resource.get().getResourceData();
    }

    public static void invalidate(String file) {
        resourcesCache.invalidate(file);
    }

    private static ResourceLoader getFileResourceLoader(String file) {
        ResourceLoader loader = DEFAULT_LOADER;
        for (var resourceLoader : resourceLoaders.entrySet()) {
            String glob = "glob:" + resourceLoader.getKey();
            PathMatcher matcher = FileSystems.getDefault().getPathMatcher(glob);

            if(matcher.matches(Path.of(file).getFileName())) {
                loader = resourceLoader.getValue();
            }
        }
        return loader;
    }

    public static void loadResourceFromEntry(ResourceEntry entry) {
        Threading.io().submit(()->{
            Path name = Path.of(entry.getName()).getFileName();
            toBeLoadedEntries.put(name, entry);
        });
    }

    public static void loadResourcesFromDirectory(String directory) {
        Threading.io().submit(()->{
            LOGGER.info("Loading Resources from directory {} to cache", directory);
            for(Path path : IoUtils.getFiles(directory)) {
                toBeLoadedResources.put(path, path.toFile());
            }
            LOGGER.info("Cached {} Resources", toBeLoadedResources.size());
        });
    }

    private static void loop() {

    }

    private static void shutdown() {

    }
}
