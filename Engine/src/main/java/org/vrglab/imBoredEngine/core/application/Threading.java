package org.vrglab.imBoredEngine.core.application;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vrglab.imBoredEngine.core.initializer.annotations.CalledDuringInit;
import org.vrglab.imBoredEngine.core.initializer.annotations.CalledDuringShutdown;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class Threading {
    private static final Logger LOGGER = LogManager.getLogger(Threading.class);

    private static final ExecutorService GENERAL_POOL = Executors.newFixedThreadPool(
            Math.max(2, Runtime.getRuntime().availableProcessors() - 1),
            r -> {
                Thread t = new Thread(r, "EngineWorker");
                t.setDaemon(true);
                return t;
            }
    );

    private static final ExecutorService IO_POOL = Executors.newSingleThreadExecutor(
            r -> new Thread(r, "IO Worker")
    );

    private static final ExecutorService RENDERER = Executors.newSingleThreadExecutor(
            r -> new Thread(r, "Renderer")
    );

    private static final ScheduledExecutorService SCHEDULER = Executors.newScheduledThreadPool(2);

    private static final ExecutorService SINGLE_BACKGROUND = Executors.newSingleThreadExecutor(
            r -> new Thread(r, "BackgroundTask")
    );

    public static ExecutorService general() {
        return GENERAL_POOL;
    }


    public static ExecutorService io() {
        return IO_POOL;
    }

    public static ExecutorService renderer() {
        return RENDERER;
    }

    public static ScheduledExecutorService scheduler() {
        return SCHEDULER;
    }

    public static ExecutorService background() {
        return SINGLE_BACKGROUND;
    }

    @CalledDuringInit(priority = 5)
    private static void init() {
        LOGGER.info("Threading system initialized with {} workers",
                Runtime.getRuntime().availableProcessors());
    }

    @CalledDuringShutdown(priority = 2)
    private static void shutdown() {
        LOGGER.info("Shutting down Threading system...");
        GENERAL_POOL.shutdownNow();
        IO_POOL.shutdownNow();
        SCHEDULER.shutdownNow();
        SINGLE_BACKGROUND.shutdownNow();
    }
}
