package org.vrglab.imBoredEngine.core.debugging;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.*;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.config.Configurator;

import java.io.Serializable;
import java.util.concurrent.ConcurrentLinkedDeque;


public class MemoryAppender extends AbstractAppender {
    private static final ConcurrentLinkedDeque<String> logs = new ConcurrentLinkedDeque<>();


    protected MemoryAppender(String name, Layout layout) {
        super(name, null, layout, false, null);
    }

    @Override
    public void append(LogEvent event) {
        String message = new String(getLayout().toByteArray(event));
        logs.add(message);
    }

    public static String getRecentLogs() {
        StringBuilder sb = new StringBuilder();
        for (String line : logs) sb.append(line);
        return sb.toString();
    }

    public static void clear() {
        logs.clear();
    }

    // register at runtime
    public static void attach() {
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        org.apache.logging.log4j.core.config.Configuration config = ctx.getConfiguration();

        PatternLayout layout = PatternLayout.newBuilder()
                .setPattern("[%d{HH:mm:ss}] [%t/%level]: %msg%n")
                .build();

        MemoryAppender appender = new MemoryAppender("MemoryAppender", layout);
        appender.start();

        LoggerConfig loggerConfig = config.getRootLogger();
        loggerConfig.addAppender(appender, null, null);

        ctx.updateLoggers();
    }
}
