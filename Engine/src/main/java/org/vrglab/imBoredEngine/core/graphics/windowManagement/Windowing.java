package org.vrglab.imBoredEngine.core.graphics.windowManagement;

import io.github.libsdl4j.api.Sdl;
import io.github.libsdl4j.api.SdlSubSystemConst;
import io.github.libsdl4j.api.event.SDL_Event;
import io.github.libsdl4j.api.video.SDL_Window;
import io.github.libsdl4j.api.video.SDL_WindowFlags;
import io.github.libsdl4j.api.video.SdlVideo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vrglab.imBoredEngine.core.game.GameLoader;
import org.vrglab.imBoredEngine.core.initializer.interfaces.CalledDuringInit;
import org.vrglab.imBoredEngine.core.initializer.interfaces.CalledDuringLoop;
import org.vrglab.imBoredEngine.core.initializer.interfaces.CalledDuringShutdown;
import org.vrglab.imBoredEngine.core.platform.AppInfo;

import static io.github.libsdl4j.api.event.SDL_EventType.SDL_QUIT;
import static io.github.libsdl4j.api.event.SdlEvents.SDL_PollEvent;
import static io.github.libsdl4j.api.SdlSubSystemConst.SDL_INIT_VIDEO;
import static io.github.libsdl4j.api.video.SDL_WindowFlags.SDL_WINDOW_RESIZABLE;
import static io.github.libsdl4j.api.video.SDL_WindowFlags.SDL_WINDOW_SHOWN;
import static io.github.libsdl4j.api.video.SdlVideo.SDL_CreateWindow;
import static io.github.libsdl4j.api.video.SdlVideoConst.SDL_WINDOWPOS_CENTERED;

public class Windowing {

    private static final Logger LOGGER = LogManager.getLogger(Windowing.class);

    private SDL_Window windowInstance;

    private WindowState state;

    private boolean sdlInitialized = false, should_shutdown = false;

    private Windowing() {
       if(Sdl.SDL_Init(SDL_INIT_VIDEO) != 0) {
           throw new RuntimeException("Failed to initialize SDL");
       } else {
           sdlInitialized = true;
       }
        LOGGER.info("SDL2 initialized");

        state = WindowState.Builder.create().canResize().fullscreen().width(1920).height(1080).build();

        windowInstance = SDL_CreateWindow(GameLoader.getAppInfo().getName(),
                SDL_WINDOWPOS_CENTERED,
                SDL_WINDOWPOS_CENTERED,
                state.getWidth(), state.getHeight(),
                state.getFlags());

        LOGGER.info("Opened Window");
    }


    public void pollEvents() {
        SDL_Event event = new SDL_Event();
        while(SDL_PollEvent(event) != 0) {
            switch(event.type) {
                case SDL_QUIT:
                    should_shutdown = true;
                    break;
            }
        }
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
