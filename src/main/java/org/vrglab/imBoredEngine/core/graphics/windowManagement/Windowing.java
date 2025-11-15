package org.vrglab.imBoredEngine.core.graphics.windowManagement;

import io.github.libsdl4j.api.Sdl;
import io.github.libsdl4j.api.SdlSubSystemConst;
import io.github.libsdl4j.api.event.SDL_Event;
import io.github.libsdl4j.api.video.SDL_Window;
import io.github.libsdl4j.api.video.SDL_WindowFlags;
import io.github.libsdl4j.api.video.SdlVideo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vrglab.imBoredEngine.core.initializer.ApplicationInitializer;
import org.vrglab.imBoredEngine.core.initializer.interfaces.CalledDuringInit;
import org.vrglab.imBoredEngine.core.initializer.interfaces.CalledDuringLoop;
import org.vrglab.imBoredEngine.core.initializer.interfaces.CalledDuringShutdown;

import static io.github.libsdl4j.api.event.SDL_EventType.SDL_QUIT;
import static io.github.libsdl4j.api.event.SdlEvents.SDL_PollEvent;
import static io.github.libsdl4j.api.SdlSubSystemConst.SDL_INIT_VIDEO;
import static io.github.libsdl4j.api.video.SDL_WindowFlags.SDL_WINDOW_RESIZABLE;
import static io.github.libsdl4j.api.video.SDL_WindowFlags.SDL_WINDOW_SHOWN;
import static io.github.libsdl4j.api.video.SdlVideo.SDL_CreateWindow;
import static io.github.libsdl4j.api.video.SdlVideoConst.SDL_WINDOWPOS_CENTERED;

public class Windowing {

    private static final Logger LOGGER = LogManager.getLogger(Windowing.class);

    private SDL_Window window_instance;

    private boolean sdl_initialized = false, should_shutdown = false;

    private Windowing() {
       if(Sdl.SDL_Init(SDL_INIT_VIDEO) != 0) {
           throw new RuntimeException("Failed to initialize SDL");
       } else {
           sdl_initialized = true;
       }
        LOGGER.info("SDL2 initialized");

        window_instance = SDL_CreateWindow("Demo SDL2",
                SDL_WINDOWPOS_CENTERED,
                SDL_WINDOWPOS_CENTERED,
                1024, 768,
                SDL_WINDOW_SHOWN | SDL_WINDOW_RESIZABLE);

        LOGGER.info("Opened Window");
    }


    public void PollEvents() {
        SDL_Event event = new SDL_Event();
        while(SDL_PollEvent(event) != 0) {
            switch(event.type) {
                case SDL_QUIT:
                    should_shutdown = true;
                    break;
            }
        }
    }

    public boolean ShouldShutdown() {
        return should_shutdown;
    }

    public void Quite() {
        if(sdl_initialized) {
            SdlVideo.SDL_DestroyWindow(window_instance);
            Sdl.SDL_Quit();
        }
    }



    private static Windowing instance;

    public static Windowing getInstance() {
        return instance;
    }


    @CalledDuringInit(priority = 2)
    private static void Initialize() {
        instance = new Windowing();
    }

    @CalledDuringLoop(priority = 0)
    private static void Loop() {
        instance.PollEvents();
    }

    @CalledDuringShutdown(priority = 0)
    private static void Shutdown() {
        instance.Quite();
    }
}
