package org.vrglab.imBoredEngine.core.scripting;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.ast.Variable;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;
import org.vrglab.imBoredEngine.core.application.AppData;
import org.vrglab.imBoredEngine.core.application.AppInfo;
import org.vrglab.imBoredEngine.core.game.GameLoader;

public class imBoredEngineLib extends TwoArgFunction {

    public imBoredEngineLib() {

    }

    @Override
    public LuaValue call(LuaValue arg1, LuaValue env) {
        LuaTable lib = new LuaTable();

        /* Replace the Lua function with Engine compatible logging system */
        env.set("print", new Debug.LogFunction());
        env.set("error", new Debug.LogErrorFunction());


        lib.set("Debug", new Debug());
        lib.set("Application", new Application());


        /*Register lib to lua globals*/
        env.set("imBoredEngine", lib);
        env.get("package").get("loaded").set("imBoredEngine", lib);
        return lib;
    }


    static class Debug extends TwoArgFunction {
        static Logger LOGGER = LogManager.getLogger("Lua App:" + GameLoader.getAppInfo().getName());

        @Override
        public LuaValue call(LuaValue arg1, LuaValue env) {
            LuaTable clas = new LuaTable();

            clas.set("log", new LogFunction());
            clas.set("logWarn", new LogWarnFunction());
            clas.set("logError", new LogErrorFunction());

            return clas;
        }

        static class LogFunction extends OneArgFunction {
            @Override
            public LuaValue call(LuaValue arg) {
                LOGGER.info(arg.tojstring());
                return NIL;
            }
        }
        static class LogWarnFunction extends OneArgFunction {
            @Override
            public LuaValue call(LuaValue arg) {
                LOGGER.warn(arg.tojstring());
                return NIL;
            }
        }

        static class LogErrorFunction extends OneArgFunction {
            @Override
            public LuaValue call(LuaValue arg) {
                LOGGER.error(arg.tojstring());
                return NIL;
            }
        }
    }

    static class Application extends TwoArgFunction {

        @Override
        public LuaValue call(LuaValue arg1, LuaValue env) {
            LuaTable clas = new LuaTable();

            clas.set("appName", LuaValue.valueOf(GameLoader.getAppInfo().getName()));
            clas.set("appAuthor", LuaValue.valueOf(GameLoader.getAppInfo().getAuthor()));
            clas.set("appVersion", LuaValue.valueOf(GameLoader.getAppInfo().getVersion()));
            clas.set("runtimePath", LuaValue.valueOf(AppData.getRuntimePath()));

            clas.set("isDebugMode", LuaValue.valueOf(AppData.isDebug()));
            clas.set("isReleaseMode", LuaValue.valueOf(AppData.isRelease()));
            clas.set("isUnitTestMode", LuaValue.valueOf(AppData.isTest()));
            clas.set("isInEditor", LuaValue.valueOf(AppData.isEditor()));

            return clas;
        }

    }
}
