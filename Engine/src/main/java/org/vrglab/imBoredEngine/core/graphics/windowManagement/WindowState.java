package org.vrglab.imBoredEngine.core.graphics.windowManagement;

import static io.github.libsdl4j.api.video.SDL_WindowFlags.*;

public class WindowState {

    private int flags;
    private int width, height;

    private WindowState(int flags, int width, int height) {
        this.flags = flags;
        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getFlags() {
        return flags;
    }


    public static class Builder {
        private int flags, width, height;

        Builder() {
            flags = SDL_WINDOW_SHOWN;
            width = 1024;
            height = 768;
        }

        public static Builder create() {
            return new Builder();
        }

        public Builder canResize() {
            flags = flags | SDL_WINDOW_RESIZABLE;
            return this;
        }

        public Builder focused() {
            flags = flags | SDL_WINDOW_MOUSE_FOCUS;
            return this;
        }

        public Builder fullscreen() {
            flags = flags | SDL_WINDOW_FULLSCREEN;
            return this;
        }

        public Builder borderless() {
            flags = flags | SDL_WINDOW_FULLSCREEN;
            return this;
        }

        public Builder highDPI() {
            flags = flags | SDL_WINDOW_ALLOW_HIGHDPI;
            return this;
        }

        public Builder minimized() {
            flags = flags | SDL_WINDOW_MINIMIZED;
            return this;
        }

        public Builder maximized() {
            flags = flags | SDL_WINDOW_MAXIMIZED;
            return this;
        }

        public Builder width(int width) {
            this.width = width;
            return this;
        }

        public Builder height(int height) {
            this.height = height;
            return this;
        }

        public Builder flags(int flags) {
            this.flags = flags;
            return this;
        }

        public WindowState build() {
            return new WindowState(flags, width, height);
        }
    }
}
