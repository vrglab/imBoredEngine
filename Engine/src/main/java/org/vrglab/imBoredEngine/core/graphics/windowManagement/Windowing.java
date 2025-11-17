package org.vrglab.imBoredEngine.core.graphics.windowManagement;

import io.github.libsdl4j.api.Sdl;
import io.github.libsdl4j.api.event.SDL_Event;
import io.github.libsdl4j.api.event.events.SDL_WindowEvent;
import io.github.libsdl4j.api.syswm.SDL_SysWMInfo;
import io.github.libsdl4j.api.video.SDL_Window;
import io.github.libsdl4j.api.video.SdlVideo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vrglab.imBoredEngine.core.debugging.CrashHandler;
import org.vrglab.imBoredEngine.core.game.GameLoader;
import org.vrglab.imBoredEngine.core.graphics.windowManagement.annotations.AfterWindowInitStep;
import org.vrglab.imBoredEngine.core.graphics.windowManagement.annotations.BeforeWindowInitStep;
import org.vrglab.imBoredEngine.core.graphics.windowManagement.annotations.WindowStateUpdatedEvent;
import org.vrglab.imBoredEngine.core.initializer.annotations.CalledDuringInit;
import org.vrglab.imBoredEngine.core.initializer.annotations.CalledDuringLoop;
import org.vrglab.imBoredEngine.core.initializer.annotations.CalledDuringShutdown;
import org.vrglab.imBoredEngine.core.utils.ReflectionsUtil;

import static io.github.libsdl4j.api.error.SdlError.SDL_GetError;
import static io.github.libsdl4j.api.event.SDL_EventType.SDL_QUIT;
import static io.github.libsdl4j.api.event.SDL_EventType.SDL_WINDOWEVENT;
import static io.github.libsdl4j.api.event.SdlEvents.SDL_PollEvent;
import static io.github.libsdl4j.api.SdlSubSystemConst.SDL_INIT_VIDEO;
import static io.github.libsdl4j.api.syswm.SdlSysWM.SDL_GetWindowWMInfo;
import static io.github.libsdl4j.api.video.SDL_WindowEventID.*;
import static io.github.libsdl4j.api.video.SdlVideo.SDL_CreateWindow;
import static io.github.libsdl4j.api.video.SdlVideoConst.SDL_WINDOWPOS_CENTERED;

public class Windowing {

    private static final Logger LOGGER = LogManager.getLogger(Windowing.class);

    private SDL_Window windowInstance;

    private SDL_SysWMInfo sdlSysWMInfoWindows;

    private WindowState state;

    private boolean sdlInitialized = false, should_shutdown = false;

    private Windowing() {
       if(Sdl.SDL_Init(SDL_INIT_VIDEO) != 0) {
           throw new RuntimeException("Failed to initialize SDL");
       } else {
           sdlInitialized = true;
       }
        LOGGER.info("SDL2 initialized");

        state = WindowState.Builder.create().canResize().width(1920).height(1080).build();

        ReflectionsUtil.findPrioritisedMethods(BeforeWindowInitStep.class).forEach(((integer, method) -> {
            try {
                method.setAccessible(true);
                method.invoke(null, this);
            } catch (Throwable e) {
                CrashHandler.HandleException(e);
            }
        }));

        windowInstance = SDL_CreateWindow(GameLoader.getAppInfo().getName(),
                SDL_WINDOWPOS_CENTERED,
                SDL_WINDOWPOS_CENTERED,
                state.getWidth(), state.getHeight(),
                state.getFlags());

        sdlSysWMInfoWindows = new SDL_SysWMInfo();

        if (!SDL_GetWindowWMInfo(windowInstance, sdlSysWMInfoWindows)) {
            throw new IllegalStateException("Failed to get SDL WM Info: " + SDL_GetError());
        }

        LOGGER.info("Opened Window");

        ReflectionsUtil.findPrioritisedMethods(AfterWindowInitStep.class).forEach(((integer, method) -> {
            try {
                method.setAccessible(true);
                method.invoke(null, this);
            } catch (Throwable e) {
                CrashHandler.HandleException(e);
            }
        }));
    }


    public void pollEvents() {
        SDL_Event event = new SDL_Event();
        while(SDL_PollEvent(event) != 0) {
            switch(event.type) {
                case SDL_QUIT:
                    should_shutdown = true;
                    break;
                case SDL_WINDOWEVENT:
                    handleWindowEvent(event.window);
                    break;
            }
        }
    }

    public SDL_Window getSDLWindow() {
        return windowInstance;
    }

    public WindowState getState() {
        return state;
    }

    public SDL_SysWMInfo getSdlSysWMInfoWindows() {
        return sdlSysWMInfoWindows;
    }

    public boolean shouldShutdown() {
        return should_shutdown;
    }

    public void quite() {
        if(instance != null) {
            SdlVideo.SDL_DestroyWindow(windowInstance);
        }

        if(sdlInitialized) {
            Sdl.SDL_Quit();
        }
    }


    private void handleWindowEvent(SDL_WindowEvent windowEvent) {
        switch(windowEvent.event) {
            case SDL_WINDOWEVENT_RESIZED:
                int newWidth = windowEvent.data1;
                int newHeight = windowEvent.data2;

                state = WindowState.Builder.create().flags(state.getFlags()).width(newWidth).height(newHeight).build();
                break;
        }

        ReflectionsUtil.findMethods(WindowStateUpdatedEvent.class).forEach((method) -> {
            try {
                method.setAccessible(true);
                method.invoke(null, windowEvent.event, state);
            } catch (Throwable e) {
                CrashHandler.HandleException(e);
            }
        });
    }

    private static Windowing instance;

    public static Windowing getInstance() {
        return instance;
    }


    @CalledDuringInit(priority = 50)
    private static void Initialize() {
        instance = new Windowing();
    }

    @CalledDuringLoop(priority = 0)
    private static void Loop() {
        instance.pollEvents();
    }

    @CalledDuringShutdown(priority = 0)
    private static void Shutdown() {
        if(instance != null) {
            instance.quite();
        }
    }
}
