package org.vrglab.imBoredEngine.core.scripting;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LoadState;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.compiler.LuaC;
import org.luaj.vm2.lib.*;
import org.luaj.vm2.lib.jse.*;
import org.luaj.vm2.luajc.LuaJC;
import org.vrglab.imBoredEngine.core.application.Threading;
import org.vrglab.imBoredEngine.core.debugging.CrashHandler;
import org.vrglab.imBoredEngine.core.initializer.annotations.CalledDuringInit;
import org.vrglab.imBoredEngine.core.utils.IoUtils;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class ScriptingEngine {
    private static Logger LOGGER = LogManager.getLogger(ScriptingEngine.class);

    static ScriptEngineManager manager;
    static ScriptEngine engine;
    static ScriptEngineFactory factory;
    static ScriptContext scriptContext;
    static Globals globals;

    static Map<InputStream, File> loadedScripts = new HashMap<>();

    static Map<String, LuaValue> compiledScripts = new HashMap<>();

    @CalledDuringInit(priority = 3)
    private static void init() {
        LOGGER.info("Starting Scripting engine");
        manager = new ScriptEngineManager();
        engine = manager.getEngineByName("luaj");
        factory = engine.getFactory();
        scriptContext = engine.getContext();

        globals = new Globals();
        globals.load(new JseBaseLib());
        globals.load(new PackageLib());
        globals.load(new JseMathLib());
        globals.load(new CoroutineLib());
        globals.load(new DebugLib());
        globals.load(new JseIoLib());
        globals.load(new JseOsLib());
        globals.load(new Bit32Lib());
        globals.load(new LuajavaLib());
        globals.load(new TableLib());
        globals.load(new StringLib());
        globals.load(new imBoredEngineLib());
        LoadState.install(globals);
        LuaC.install(globals);

        LOGGER.info("=== Scripting Engine Summery ===");
        LOGGER.info("Engine Name: {}", factory.getEngineName());
        LOGGER.info("Engine Version: {}", factory.getEngineVersion());
        LOGGER.info("Language Name: {}", factory.getLanguageName());
        LOGGER.info("Language Version: {}", factory.getLanguageVersion());
        LOGGER.info("===============================");


        LOGGER.info("Loading cached Scripts into Lua Global");

        loadedScripts.forEach((in, file) -> {
            compiledScripts.put(file.getName(), compile(in, file));
        });

        callIfExists("init");

        LOGGER.info("Compiled scripts Successfully: {}", compiledScripts.size());
    }

    private static LuaTable compile(InputStream in, File file) {
        String source = IoUtils.streamToString(in);

        LuaTable env = new LuaTable();
        LuaTable meta = new LuaTable();
        meta.set(LuaValue.INDEX, globals);
        env.setmetatable(meta);

        LuaValue chunk = globals.load(source, file.getName(), env);
        chunk.call();

        return env;
    }

    public static void LoadScriptsFromDirectory(String directory) {
        Threading.io().submit(() -> {
            LOGGER.info("Loading Scripts from directory {} to cache", directory);
            for(Path path : IoUtils.getFiles(directory)) {
                try {
                    if(path.toFile().getName().endsWith(".lua")) {
                        loadedScripts.put(Files.newInputStream(path), path.toFile());
                    }
                } catch (IOException e) {
                    CrashHandler.HandleException(e);
                }
            }

            LOGGER.info("Cached {} Scripts", loadedScripts.size());
        });
    }

    public static void callIfExists(String functionName, LuaValue... args) {
        try {
            compiledScripts.forEach((file, env) -> {
                LuaValue func = env.get(functionName);
                if (func.isfunction()) {
                    try {
                        func.invoke(LuaValue.varargsOf(args));
                    } catch (Throwable e) {
                        CrashHandler.HandleException(e);
                    }
                }
            });
        } catch (Exception e) {
            LOGGER.error("Error calling Lua function '{}'", functionName, e);
        }
    }
}
