package org.vrglab.imBoredEngine.core.resourceManagment.resourceTypes;

import org.vrglab.imBoredEngine.core.audio.AudioManager;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import static org.lwjgl.openal.AL10.AL_FORMAT_MONO16;
import static org.lwjgl.openal.AL10.AL_FORMAT_STEREO16;

public class Audio {
    private ByteBuffer audioData;
    private ShortBuffer pcm;
    private int channels, sampleRate, samples, sourceID;

    private Audio(ByteBuffer audioData, int channels, ShortBuffer pcm, int sampleRate, int samples) {
        this.audioData = audioData;
        this.channels = channels;
        this.pcm = pcm;
        this.sampleRate = sampleRate;
        this.samples = samples;

        sourceID = AudioManager.prepareSource(this);
    }

    public ByteBuffer getAudioData() {
        return audioData;
    }

    public int getChannels() {
        return channels;
    }

    public int getSampleRate() {
        return sampleRate;
    }

    public int getSamples() {
        return samples;
    }

    public ShortBuffer getPcm() {
        return pcm;
    }

    public int getALFormat() {
        return channels == 1 ? AL_FORMAT_MONO16 : AL_FORMAT_STEREO16;
    }

    public int getSourceID() {
        return sourceID;
    }

    public static class Builder {
        private ByteBuffer audioData;
        private ShortBuffer pcm;
        private int channels, sampleRate, samples;

        Builder() {
        }

        public static Audio.Builder create() {
            return new Audio.Builder();
        }

        public Audio.Builder setAudioData(ByteBuffer audioData) {
            this.audioData = audioData;
            return this;
        }

        public Audio.Builder setChannels(int channels) {
            this.channels = channels;
            return this;
        }


        public Audio.Builder setPcm(ShortBuffer pcm) {
            this.pcm = pcm;
            return this;
        }

        public Audio.Builder setSampleRate(int sampleRate) {
            this.sampleRate = sampleRate;
            return this;
        }

        public Audio.Builder setSamples(int samples) {
            this.samples = samples;
            return this;
        }

        public Audio build() {
            return new Audio(audioData, channels, pcm, sampleRate, samples);
        }
    }
}
