package org.vrglab.imBoredEngine.core.graphics.rendering;


import com.sun.jna.Pointer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.bgfx.*;
import org.vrglab.imBoredEngine.core.application.Threading;
import org.vrglab.imBoredEngine.core.debugging.CrashHandler;
import org.vrglab.imBoredEngine.core.graphics.rendering.annotations.CalledAfterBGFXInit;
import org.vrglab.imBoredEngine.core.graphics.rendering.annotations.CalledDuringRenderLoop;
import org.vrglab.imBoredEngine.core.graphics.rendering.annotations.CalledDuringRenderLoopPrioritized;
import org.vrglab.imBoredEngine.core.graphics.windowManagement.WindowState;
import org.vrglab.imBoredEngine.core.graphics.windowManagement.annotations.AfterWindowInitStep;
import org.vrglab.imBoredEngine.core.graphics.windowManagement.Windowing;
import org.vrglab.imBoredEngine.core.graphics.windowManagement.annotations.WindowStateUpdatedEvent;
import org.vrglab.imBoredEngine.core.initializer.annotations.CalledDuringShutdown;
import org.vrglab.imBoredEngine.core.utils.ReflectionsUtil;

import static io.github.libsdl4j.api.video.SDL_WindowEventID.SDL_WINDOWEVENT_RESIZED;
import static org.lwjgl.bgfx.BGFX.*;
import static org.lwjgl.system.MemoryUtil.NULL;


public class BgfxManager {

    private static final Logger LOGGER = LogManager.getLogger(BgfxManager.class);

    static BGFXInit init = null;

    static Windowing _window;
    static BGFXCaps caps;
    static BGFXStats stats;

    @AfterWindowInitStep(priority = 0)
    private static void afterWindowInit(Windowing window) {
        LOGGER.info("Starting Rendering Engine");
        _window = window;

        BGFXPlatformData platformData = BGFXPlatformData.calloc();
        platformData.ndt(NULL);
        platformData.nwh(Pointer.nativeValue(window.getSdlSysWMInfoWindows().info.win.window));
        platformData.context(NULL);
        platformData.backBuffer(NULL);
        platformData.backBufferDS(NULL);


        BGFXInit init = BGFXInit.calloc();
        init.type(BGFX_RENDERER_TYPE_COUNT);
        init.resolution(BGFXResolution.calloc()
                .width(window.getState().getWidth())
                .height(window.getState().getHeight())
                .reset(BGFX_RESET_VSYNC));
        init.platformData(platformData);

        Threading.renderer().submit(()->{
            if (!bgfx_init(init)) {
                throw new RuntimeException("Failed to initialize bgfx!");
            }

            init.free();
            caps = bgfx_get_caps();
            stats = bgfx_get_stats();

            LOGGER.info("=== Rendering Engine Summery ===");
            LOGGER.info("Renderer: {}", bgfx_get_renderer_name(bgfx_get_renderer_type()));
            LOGGER.info("BGFX Version: {}", BGFX_API_VERSION);
            LOGGER.info("GPU Vendor ID: {}",  caps.vendorId());
            LOGGER.info("GPU Device ID: {}",  caps.deviceId());
            LOGGER.info("Used GPU Memory: {}",  stats.gpuMemoryUsed() / 1024 + " KB");
            LOGGER.info("Max GPU Memory: {}",  stats.gpuMemoryMax() / 1024 + " KB");
            LOGGER.info("Max Texture Size: {}",  caps.limits().maxTextureSize());
            LOGGER.info("Max Render Targets: {}",  caps.limits().maxFBAttachments());
            LOGGER.info("Max Uniforms: {}", caps.limits().maxUniforms());
            LOGGER.info("Supports Instancing: {}",  (caps.supported() & BGFX.BGFX_CAPS_INSTANCING) != 0);
            LOGGER.info("Supports Compute Shaders: {}" , (caps.supported() & BGFX.BGFX_CAPS_COMPUTE) != 0);
            LOGGER.info("===============================");
        });

        Threading.general().submit(()->{
            ReflectionsUtil.findMethods(CalledAfterBGFXInit.class).forEach((method) -> {
                try {
                    method.setAccessible(true);
                    method.invoke(null);
                } catch (Throwable e) {
                    CrashHandler.HandleException(e);
                }
            });
        });

    }

    @WindowStateUpdatedEvent
    private static void windowStateUpdated(byte event, WindowState windowState) {
        if(event == SDL_WINDOWEVENT_RESIZED) {
            bgfx_reset(windowState.getWidth(), windowState.getHeight(), BGFX_RESET_NONE, BGFX_BACKBUFFER_RATIO_EQUAL);
        }
    }

    public static void render() {
        bgfx_set_view_rect(0, 0, 0,
                (short) _window.getState().getWidth(),
                (short) _window.getState().getHeight());


        bgfx_set_view_clear(
                0,
                (short)(BGFX_CLEAR_COLOR | BGFX_CLEAR_DEPTH),
                0xFF3366FF,
                1.0f,
                0
        );

        Threading.renderer().submit(()->{
            ReflectionsUtil.findPrioritisedMethods(CalledDuringRenderLoopPrioritized.class).forEach((priority, method) -> {
                try {
                    method.setAccessible(true);
                    method.invoke(null);
                } catch (Throwable e) {
                    CrashHandler.HandleException(e);
                }
            });

            ReflectionsUtil.findMethods(CalledDuringRenderLoop.class).forEach((method) -> {
                try {
                    method.setAccessible(true);
                    method.invoke(null);
                } catch (Throwable e) {
                    CrashHandler.HandleException(e);
                }
            });
        });

        bgfx_touch(0);
        bgfx_frame(false);
    }

    @CalledDuringShutdown(priority = 1)
    private static void shutdown() {
        LOGGER.info("Shutting down Renderer Engine");
        bgfx_shutdown();
    }
}
