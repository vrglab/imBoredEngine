package org.vrglab.imBoredEngine.core.initializer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vrglab.imBoredEngine.core.application.Threading;
import org.vrglab.imBoredEngine.core.debugging.MemoryAppender;
import org.vrglab.imBoredEngine.core.graphics.rendering.BgfxManager;
import org.vrglab.imBoredEngine.core.graphics.windowManagement.Windowing;
import org.vrglab.imBoredEngine.core.initializer.annotations.CalledDuringInit;
import org.vrglab.imBoredEngine.core.initializer.annotations.CalledDuringLoop;
import org.vrglab.imBoredEngine.core.initializer.annotations.CalledDuringShutdown;
import org.vrglab.imBoredEngine.core.debugging.CrashHandler;
import org.vrglab.imBoredEngine.core.utils.ReflectionsUtil;

import java.lang.reflect.Method;
import java.util.Map;

public class ApplicationInitializer {

    private static final Logger LOGGER = LogManager.getLogger(ApplicationInitializer.class);

    public static void app() {
        initEngine();

        Map<Integer, Method> loop_methods = ReflectionsUtil.findPrioritisedMethods(CalledDuringLoop.class);

        while (!Windowing.getInstance().shouldShutdown()){
            loop_methods.forEach((priority, method) -> {
                try {
                    method.setAccessible(true);
                    method.invoke(null);
                } catch (Throwable e) {
                    CrashHandler.HandleException(e);
                }
            });

            try {
                Threading.general().submit(() -> {}).get();
            } catch (Throwable e) {
                CrashHandler.HandleException(e);
            }

            BgfxManager.render();

        }

        Shutdown();
    }

    public static void Shutdown() {
        Shutdown(0);
    }

    public static void Shutdown(int status) {
        LOGGER.info("===============================");
        LOGGER.info("Shutting down...");
        shutdownEngine();
        System.exit(status);
    }

    private static void initEngine() {
        MemoryAppender.attach();
        ReflectionsUtil.findPrioritisedMethods(CalledDuringInit.class).forEach((priority, method) -> {
            try {
                method.setAccessible(true);
                method.invoke(null);
            } catch (Throwable e) {
                CrashHandler.HandleException(e);
            }
        });
    }


    private static void shutdownEngine() {
        ReflectionsUtil.findPrioritisedMethods(CalledDuringShutdown.class).forEach((priority, method) -> {
            try {
                method.setAccessible(true);
                method.invoke(null);
            } catch (Throwable e) {
                CrashHandler.HandleException(e);
            }
        });
    }
}
