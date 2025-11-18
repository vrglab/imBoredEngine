package org.vrglab.imBoredEngine.core.resourceManagment;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vrglab.imBoredEngine.core.application.Threading;
import org.vrglab.imBoredEngine.core.debugging.CrashHandler;
import org.vrglab.imBoredEngine.core.initializer.ApplicationInitializer;
import org.vrglab.imBoredEngine.core.initializer.annotations.CalledDuringInit;
import org.vrglab.imBoredEngine.core.resourceManagment.Annotations.ResourceLoaders;
import org.vrglab.imBoredEngine.core.resourceManagment.interfaces.ResourceLoader;
import org.vrglab.imBoredEngine.core.utils.ReflectionsUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResourceManager {

    private static final Logger LOGGER = LogManager.getLogger(ResourceManager.class);

    private static Map<String, ResourceLoader> resourceLoaders = new HashMap();
    private static Cache<String, Resource> resourcesCache = CacheBuilder.newBuilder().build();

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


    @CalledDuringInit(priority = 6)
    private static void init() {
        LOGGER.info("Starting Resource Manager");
       Set<Class<?>> resourcesLoadersFoundUninitialized = ReflectionsUtil.findClasses(ResourceLoaders.class);

        resourcesLoadersFoundUninitialized.forEach(resourceLoadersFoundUninitialized -> {
            ResourceLoader loader = (ResourceLoader)ReflectionsUtil.createInstance(resourceLoadersFoundUninitialized);
            resourceLoaders.put(loader.getResourceRegex(), loader);
        });
    }

    public static <T> Resource<T> loadResource(String file) {
        if(resourcesCache.asMap().containsKey(file)) {
            return resourcesCache.getIfPresent(file);
        }
        File f = new File(file);
        ByteArrayOutputStream  outputStream = new ByteArrayOutputStream();
        Threading.io().submit(() -> {
            try {
                outputStream.write(Files.readAllBytes(f.toPath()));
            } catch (IOException e) {
                CrashHandler.HandleException(e);
            }
        });
        ResourceLoader<T> loader = getFileResourceLoader(f.getAbsolutePath());
        T loaded = loader.load(outputStream.toByteArray());
        Resource<T> constructedResource = new Resource<T>(outputStream.toByteArray(),loaded, f, f.getName());
        resourcesCache.put(file, constructedResource);
        return constructedResource;
    }

    public static void invalidate(String file) {
        resourcesCache.invalidate(file);
    }

    private static ResourceLoader getFileResourceLoader(String file) {
        ResourceLoader loader = DEFAULT_LOADER;
        for (var resourceLoader : resourceLoaders.entrySet()) {
            Pattern pattern = Pattern.compile(resourceLoader.getKey());
            Matcher matcher = pattern.matcher(file);

            if(matcher.find()) {
                loader = resourceLoader.getValue();
            }
        }
        return loader;
    }

    private static void loop() {

    }

    private static void shutdown() {

    }
}
