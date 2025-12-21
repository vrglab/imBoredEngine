package org.vrglab.imBoredEngine.core.resourceManagment.loaders.audio;

import org.lwjgl.stb.STBVorbis;
import org.lwjgl.stb.STBVorbisInfo;
import org.lwjgl.system.MemoryStack;
import org.vrglab.imBoredEngine.core.resourceManagment.Annotations.ResourceLoaders;
import org.vrglab.imBoredEngine.core.resourceManagment.interfaces.ResourceLoader;
import org.vrglab.imBoredEngine.core.resourceManagment.resourceTypes.Audio;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import static org.lwjgl.system.MemoryUtil.memAllocShort;

@ResourceLoaders
public class OggLoader extends ResourceLoader<Audio> {

    @Override
    public String getResourceRegex() {
        return "*.ogg";
    }

    @Override
    public Audio load(byte[] fileContent) {
        ByteBuffer fileDataBuffer = ByteBuffer.wrap(fileContent);
        int channels, sampleRate, samples;
        ShortBuffer pcm;

        try(MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer errorBuffer = stack.mallocInt(1);
            long decoder = STBVorbis.stb_vorbis_open_memory(fileDataBuffer, errorBuffer, null);


            STBVorbisInfo info = STBVorbisInfo.mallocStack(stack);
            STBVorbis.stb_vorbis_get_info(decoder, info);

            channels = info.channels();
            sampleRate = info.sample_rate();
            samples = STBVorbis.stb_vorbis_stream_length_in_samples(decoder);
            pcm = memAllocShort(samples * channels);
            STBVorbis.stb_vorbis_get_samples_short_interleaved(decoder, channels, pcm);
            STBVorbis.stb_vorbis_close(decoder);
        }

        return Audio.Builder.create().setChannels(channels).setSampleRate(sampleRate).setSamples(samples).setPcm(pcm).setAudioData(fileDataBuffer).build();
    }
}
