package org.vrglab.imBoredEngine.core.resourceManagment.loaders.audio;

import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.SampleBuffer;
import org.vrglab.imBoredEngine.core.resourceManagment.Annotations.ResourceLoaders;
import org.vrglab.imBoredEngine.core.resourceManagment.interfaces.ResourceLoader;
import org.vrglab.imBoredEngine.core.resourceManagment.resourceTypes.Audio;

import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.lwjgl.system.MemoryUtil.memAlloc;

@ResourceLoaders
public class Mp3Loader extends ResourceLoader<Audio> {
    @Override
    public String getResourceRegex() {
        return "*.mp3";
    }

    @Override
    public Audio load(byte[] fileContent) {
        try {
            ByteBuffer fileDataBuffer = memAlloc(fileContent.length);
            fileDataBuffer.put(fileContent).flip();
            Bitstream bitstream = new Bitstream(new ByteArrayInputStream(fileContent));
            Decoder decoder = new Decoder();

            List<short[]> chunks = new ArrayList<>();
            int totalSamples = 0;
            SampleBuffer output;
            int channels = 0;
            int sampleRate = 0;

            Header header;
            while ((header = bitstream.readFrame()) != null) {
                output = (SampleBuffer) decoder.decodeFrame(header, bitstream);
                short[] samples = output.getBuffer();

                chunks.add(Arrays.copyOf(samples, samples.length));
                totalSamples += samples.length;
                channels = output.getChannelCount();
                sampleRate = output.getSampleFrequency();

                bitstream.closeFrame();
            }

            bitstream.close();


            short[] pcm = new short[totalSamples];
            int pos = 0;
            for (short[] chunk : chunks) {
                System.arraycopy(chunk, 0, pcm, pos, chunk.length);
                pos += chunk.length;
            }


            ByteBuffer pcmBuffer = memAlloc(pcm.length * 2);
            ShortBuffer shortView = pcmBuffer.asShortBuffer();
            shortView.put(pcm).flip();

            return Audio.Builder.create()
                    .setAudioData(fileDataBuffer)
                    .setChannels(channels)
                    .setSampleRate(sampleRate)
                    .setSamples(pcm.length)
                    .setPcm(shortView)
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Failed to decode MP3", e);
        }
    }
}
