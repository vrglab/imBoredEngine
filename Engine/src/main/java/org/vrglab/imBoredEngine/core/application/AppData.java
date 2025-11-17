package org.vrglab.imBoredEngine.core.application;

import com.google.common.base.Suppliers;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vrglab.imBoredEngine.core.debugging.CrashHandler;
import org.vrglab.imBoredEngine.core.initializer.annotations.CalledDuringInit;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Supplier;

import static org.vrglab.imBoredEngine.core.utils.IoUtils.hasFile;

public class AppData {

    private static final Logger LOGGER = LogManager.getLogger(AppData.class);

    // Memoized suppliers
    private static final Supplier<Boolean> DEBUG_MODE = Suppliers.memoize(AppData::detectDebugMode);
    private static final Supplier<Boolean> TEST_ENV = Suppliers.memoize(AppData::detectJUnitEnv);
    private static final Supplier<Boolean> RELEASE_MODE = Suppliers.memoize(() -> !isDebug() && !isTest());
    private static final Supplier<Boolean> EDITOR_MODE = Suppliers.memoize(() -> isEditorMode());
    private static final Supplier<String> RUNTIME_PATH = Suppliers.memoize(AppData::detectRuntimePath);
    private static final Supplier<String> VERSION = Suppliers.memoize(AppData::detectVersion);
    private static final Supplier<String> JVM_INFO = Suppliers.memoize(AppData::detectJvmInfo);
    private static final Supplier<String> JVM_VENDOR = Suppliers.memoize(() -> System.getProperty("java.vendor"));
    private static final Supplier<String> OS_INFO = Suppliers.memoize(AppData::detectOsInfo);
    private static final Supplier<String> USER_LOCALE = Suppliers.memoize(() -> Locale.getDefault().toString());
    private static final Supplier<Integer> CPU_CORES = Suppliers.memoize(() -> Runtime.getRuntime().availableProcessors());
    private static final Supplier<Long> MAX_MEMORY = Suppliers.memoize(() -> Runtime.getRuntime().maxMemory());
    private static final Supplier<Long> TOTAL_MEMORY = Suppliers.memoize(() -> Runtime.getRuntime().totalMemory());
    private static final Supplier<Long> FREE_MEMORY = Suppliers.memoize(() -> Runtime.getRuntime().freeMemory());
    private static final Supplier<Boolean> DOCKER_ENV = Suppliers.memoize(AppData::detectDocker);

    private AppData() {} // static utility class

    // ------------------------------------------------------------------------
    // Public API
    // ------------------------------------------------------------------------

    public static boolean isDebug() { return DEBUG_MODE.get(); }
    public static boolean isTest() { return TEST_ENV.get(); }
    public static boolean isRelease() { return RELEASE_MODE.get(); }
    public static boolean isEditor() { return EDITOR_MODE.get(); }
    public static String getRuntimePath() { return RUNTIME_PATH.get(); }
    public static String getVersion() { return VERSION.get(); }
    public static String getJvmInfo() { return JVM_INFO.get(); }
    public static String getJvmVendor() { return JVM_VENDOR.get(); }
    public static String getOsInfo() { return OS_INFO.get(); }
    public static String getUserLocale() { return USER_LOCALE.get(); }
    public static int getCpuCores() { return CPU_CORES.get(); }
    public static long getMaxMemory() { return MAX_MEMORY.get(); }
    public static long getTotalMemory() { return TOTAL_MEMORY.get(); }
    public static long getFreeMemory() { return FREE_MEMORY.get(); }
    public static boolean isDocker() { return DOCKER_ENV.get(); }

    // ------------------------------------------------------------------------
    // Detection Logic
    // ------------------------------------------------------------------------

    private static boolean detectDebugMode() {
        // system property override
        String sysProp = System.getProperty("app.env", "").toLowerCase(Locale.ROOT);
        if (sysProp.contains("debug")) return true;

        // JVM debug arguments
        boolean hasDebugArg = ManagementFactory.getRuntimeMXBean()
                .getInputArguments().stream()
                .anyMatch(arg -> arg.contains("jdwp") || arg.contains("-Xdebug"));
        if (hasDebugArg) return true;

        // DEV_MODE file
        if (Files.exists(Path.of("DEV_MODE"))) return true;

        // IDE detection
        String userDir = System.getProperty("user.dir", "");
        return userDir.toLowerCase(Locale.ROOT).contains("idea") ||
                userDir.toLowerCase(Locale.ROOT).contains("eclipse");
    }

    private static boolean detectJUnitEnv() {
        try {
            for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {
                String cls = ste.getClassName();
                if (cls.startsWith("org.junit.") || cls.startsWith("org.mockito.")) {
                    return true;
                }
            }
            Class.forName("org.junit.jupiter.api.Test");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private static String detectRuntimePath() {
        try {
            Path path = Path.of(AppData.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI());
            if (Files.isDirectory(path)) {
                return path.toAbsolutePath().toString();
            } else {
                return path.getParent().toAbsolutePath().toString();
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to resolve runtime path", e);
            return System.getProperty("user.dir", ".");
        }
    }

    private static String detectVersion() {
        return Optional.ofNullable(AppData.class.getPackage())
                .map(Package::getImplementationVersion)
                .orElse("dev");
    }

    private static String detectJvmInfo() {
        return String.format("%s (%s)", System.getProperty("java.vm.name"), System.getProperty("java.version"));
    }

    private static String detectOsInfo() {
        return String.format("%s %s (%s)",
                System.getProperty("os.name"),
                System.getProperty("os.version"),
                System.getProperty("os.arch"));
    }

    private static boolean detectDocker() {
        Path cgroup = Path.of("/proc/self/cgroup");
        try {
            return Files.exists(cgroup) && Files.readString(cgroup).contains("docker");
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean isEditorMode() {
        try {
            return !hasFile(Path.of(AppData.getRuntimePath()), "*.vibe");
        } catch (IOException e) {
            CrashHandler.HandleException(e);
            return false;
        }
    }

    // ------------------------------------------------------------------------
    // Utility / Logging
    // ------------------------------------------------------------------------

    @CalledDuringInit(priority = 1)
    public static void logEnvironmentSummary() {
        LOGGER.info("=== Engine Environment Summary ===");
        LOGGER.info("Mode: {}", isTest() ? "TEST" : isDebug() ? "DEBUG" : "RELEASE");
        LOGGER.info("Editor Environment: {}", isEditor());
        LOGGER.info("Runtime Path: {}", getRuntimePath());
        LOGGER.info("Version: {}", getVersion());
        LOGGER.info("JVM: {} ({})", getJvmInfo(), getJvmVendor());
        LOGGER.info("OS: {}", getOsInfo());
        LOGGER.info("Locale: {}", getUserLocale());
        LOGGER.info("CPU Cores: {}", getCpuCores());
        LOGGER.info("Memory (free/total/max MB): {}/{}/{}", getFreeMemory() / (1024*1024),
                getTotalMemory() / (1024*1024), getMaxMemory() / (1024*1024));
        LOGGER.info("Docker: {}", isDocker());
        LOGGER.info("===============================");
    }
}
