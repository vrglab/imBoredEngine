package org.vrglab.imBoredEngine.core.initializer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.vrglab.imBoredEngine.core.application.Threading;
import org.vrglab.imBoredEngine.core.debugging.MemoryAppender;
import org.vrglab.imBoredEngine.core.graphics.windowManagement.Windowing;
import org.vrglab.imBoredEngine.core.initializer.interfaces.CalledDuringInit;
import org.vrglab.imBoredEngine.core.initializer.interfaces.CalledDuringLoop;
import org.vrglab.imBoredEngine.core.initializer.interfaces.CalledDuringShutdown;
import org.vrglab.imBoredEngine.core.debugging.CrashHandler;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;

public class ApplicationInitializer {

    private static final Logger LOGGER = LogManager.getLogger(ApplicationInitializer.class);

    private final static String PACKAGES = "org.vrglab.imBoredEngine";

    static Reflections reflections = new Reflections(
            new ConfigurationBuilder()
                    .forPackage(PACKAGES)
                    .filterInputsBy(new FilterBuilder().includePackage(PACKAGES))
                    .setScanners(Scanners.MethodsAnnotated)
    );


    public static void app() {
        initEngine();

        Map<Integer, Method> loop_methods = initLoop();

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
            } catch (InterruptedException e) {
                CrashHandler.HandleException(e);
            } catch (ExecutionException e) {
                CrashHandler.HandleException(e);
            }

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
        Set<Method> methods = reflections.getMethodsAnnotatedWith(CalledDuringInit.class);

        Map<Integer, Method> methodMap = new TreeMap<>();

        for (Method method : methods) {
            CalledDuringInit annotation = method.getAnnotation(CalledDuringInit.class);
            methodMap.put(annotation.priority(), method);
        }

        methodMap.forEach((priority, method) -> {
            try {
                method.setAccessible(true);
                method.invoke(null);
            } catch (Throwable e) {
                CrashHandler.HandleException(e);
            }
        });
    }

    public static Map<Integer, Method> initLoop() {
        Set<Method> methods = reflections.getMethodsAnnotatedWith(CalledDuringLoop.class);

        Map<Integer, Method> methodMap = new TreeMap<>();

        for (Method method : methods) {
            CalledDuringLoop annotation = method.getAnnotation(CalledDuringLoop.class);
            methodMap.put(annotation.priority(), method);
        }

        return methodMap;
    }


    private static void shutdownEngine() {
        Set<Method> methods = reflections.getMethodsAnnotatedWith(CalledDuringShutdown.class);

        Map<Integer, Method> methodMap = new TreeMap<>();

        for (Method method : methods) {
            CalledDuringShutdown annotation = method.getAnnotation(CalledDuringShutdown.class);
            methodMap.put(annotation.priority(), method);
        }

        methodMap.forEach((priority, method) -> {
            try {
                method.setAccessible(true);
                method.invoke(null);
            } catch (Throwable e) {
                CrashHandler.HandleException(e);
            }
        });
    }
}
