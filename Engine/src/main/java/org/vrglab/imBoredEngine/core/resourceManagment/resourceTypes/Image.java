package org.vrglab.imBoredEngine.core.resourceManagment.resourceTypes;

import java.nio.ByteBuffer;


public class Image {
    private ByteBuffer imageData;
    private int width, height, channels, handle;

    private Image(int channels, int handle, int height, ByteBuffer imageData, int width) {
        this.channels = channels;
        this.handle = handle;
        this.height = height;
        this.imageData = imageData;
        this.width = width;
    }

    public int getChannels() {
        return channels;
    }

    public int getHandle() {
        return handle;
    }

    public int getHeight() {
        return height;
    }

    public ByteBuffer getImageData() {
        return imageData;
    }

    public int getWidth() {
        return width;
    }


    public static class Builder {
        private ByteBuffer imageData;
        private int width, height, channels, handle;

        Builder() {
        }

        public static Builder create() {
            return new Builder();
        }

        public Builder width(int width) {
            this.width = width;
            return this;
        }

        public Builder height(int height) {
            this.height = height;
            return this;
        }

        public Builder channels(int channels) {
            this.channels = channels;
            return this;
        }

        public Builder handle(int handle) {
            this.handle = handle;
            return this;
        }

        public Builder data(ByteBuffer data) {
            this.imageData = data;
            return this;
        }


        public Image build() {
            return new Image(channels, handle, height, imageData, width);
        }
    }
}
