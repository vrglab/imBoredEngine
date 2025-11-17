package org.vrglab.imBoredEngine.core.debugging;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vrglab.imBoredEngine.core.game.GameLoader;
import org.vrglab.imBoredEngine.core.initializer.ApplicationInitializer;
import org.vrglab.imBoredEngine.core.initializer.interfaces.CalledDuringInit;
import org.vrglab.imBoredEngine.core.application.AppData;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CrashHandler {

    private static final Logger LOGGER = LogManager.getLogger(CrashHandler.class);

    private CrashHandler() {}

    /**
     * Installs the global crash handler for all threads and the main thread.
     */
    @CalledDuringInit(priority = 0)
    public static void install() {
        Thread.setDefaultUncaughtExceptionHandler(CrashHandler::handleUncaught);
        Thread.currentThread().setUncaughtExceptionHandler(CrashHandler::handleUncaught);
    }

    /**
     * Handles uncaught exceptions.
     */
    private static void handleUncaught(Thread t, Throwable e) {
        LOGGER.fatal("=== UNCAUGHT EXCEPTION IN THREAD '{}' ===", t.getName(), e);
        saveCrashDump(t, e);
        saveLogs();
        ApplicationInitializer.Shutdown(1);
    }

    /**
     * Handles caught exceptions.
     */
    public static void HandleException(Throwable e) {
        if (e instanceof Error || e instanceof IllegalStateException) {
            LOGGER.fatal("=== CAUGHT FETAL EXCEPTION IN THREAD '{}' ===", Thread.currentThread().getName(), e);
            saveCrashDump(Thread.currentThread(), e);
            saveLogs();
            ApplicationInitializer.Shutdown(1);
        } else {
            LOGGER.error("=== CAUGHT EXCEPTION IN THREAD '{}' ===", Thread.currentThread().getName(), e);
        }
    }

    /**
     * Saves a crash dump to disk.
     */
    private static void saveCrashDump(Thread t, Throwable e) {
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            Path dumpDir = Path.of(AppData.getRuntimePath() + "/crash_dumps");
            if (!Files.exists(dumpDir)) {
                Files.createDirectories(dumpDir);
            }

            Path dumpFile = dumpDir.resolve("crash_" + timestamp + ".log");

            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));

            StringBuilder report = new StringBuilder();
            report.append("=== Fetal Error ===\n");
            report.append("Opsies there was fetal crash\n\n");

            report.append("=== Vrglabs Engine Crash Report ===\n");
            report.append("Timestamp: ").append(timestamp).append("\n");
            report.append("Thread: ").append(t.getName()).append("\n\n");
            report.append("--- Environment ---\n");
            report.append("Mode: ").append(AppData.isTest() ? "TEST" :
                    AppData.isDebug() ? "DEBUG" : "RELEASE").append("\n");
            report.append("Runtime Path: ").append(AppData.getRuntimePath()).append("\n");
            report.append("Version: ").append(AppData.getVersion()).append("\n");
            report.append("JVM: ").append(AppData.getJvmInfo()).append(" (").append(AppData.getJvmVendor()).append(")\n");
            report.append("OS: ").append(AppData.getOsInfo()).append("\n");
            report.append("CPU Cores: ").append(AppData.getCpuCores()).append("\n");
            report.append("Memory (free/total/max MB): ")
                    .append(AppData.getFreeMemory() / (1024*1024)).append("/")
                    .append(AppData.getTotalMemory() / (1024*1024)).append("/")
                    .append(AppData.getMaxMemory() / (1024*1024)).append("\n");
            report.append("Docker: ").append(AppData.isDocker()).append("\n");
            try {
                report.append("Application name: ").append(GameLoader.getAppInfo().getName()).append("\n");
                report.append("Application author: ").append(GameLoader.getAppInfo().getAuthor()).append("\n");
                report.append("Application version: ").append(GameLoader.getAppInfo().getVersion()).append("\n");
            }catch (Throwable e1) {

            }

            report.append("===============================").append("\n\n");
            report.append("--- Stack Trace ---\n");
            report.append(sw.toString());

            Files.writeString(dumpFile, report.toString());
            LOGGER.warn("Crash dump saved to {}", dumpFile.toAbsolutePath());

        } catch (IOException ioEx) {
            LOGGER.error("Failed to save crash dump", ioEx);
        }
    }

    private static void saveLogs() {
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            Path dumpDir = Path.of(AppData.getRuntimePath() + "/logs");
            if (!Files.exists(dumpDir)) {
                Files.createDirectories(dumpDir);
            }

            Path dumpFile = dumpDir.resolve(timestamp + ".log");

            StringBuilder report = new StringBuilder();
            report.append("\n--- Recent Logs ---\n");
            report.append(MemoryAppender.getRecentLogs());
            MemoryAppender.clear();

            Files.writeString(dumpFile, report.toString());
            LOGGER.warn("Crash dump saved to {}", dumpFile.toAbsolutePath());

        } catch (IOException ioEx) {
            LOGGER.error("Failed to save crash dump", ioEx);
        }
    }
}
