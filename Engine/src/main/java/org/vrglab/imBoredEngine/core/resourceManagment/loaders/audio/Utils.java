package org.vrglab.imBoredEngine.core.resourceManagment.loaders.audio;

import org.vrglab.imBoredEngine.core.resourceManagment.resourceTypes.Audio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;

import static org.lwjgl.system.MemoryUtil.memAlloc;
import static org.lwjgl.system.MemoryUtil.memAllocShort;

public class Utils {

    public static Audio buildWithInternalJavaSystem(byte[] fileContent, String formatName) {
        try {
            AudioInputStream ais = AudioSystem.getAudioInputStream(new ByteArrayInputStream(fileContent));
            AudioFormat baseFormat = ais.getFormat();

            AudioFormat format = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    baseFormat.getSampleRate(),
                    16,
                    baseFormat.getChannels(),
                    baseFormat.getChannels() * 2,
                    baseFormat.getSampleRate(),
                    false
            );

            AudioInputStream pcmStream = AudioSystem.getAudioInputStream(format, ais);
            byte[] audioBytes = pcmStream.readAllBytes();

            int channels = format.getChannels();
            int sampleRate = (int) format.getSampleRate();
            int samples = audioBytes.length / 2;

            ShortBuffer pcm = memAllocShort(samples);
            for (int i = 0; i < samples; i++) {
                int low = audioBytes[2 * i] & 0xff;
                int high = audioBytes[2 * i + 1];
                pcm.put((short) ((high << 8) | low));
            }
            pcm.flip();

            ByteBuffer fileDataBuffer = memAlloc(fileContent.length);
            fileDataBuffer.put(fileContent).flip();

            return Audio.Builder.create()
                    .setAudioData(fileDataBuffer)
                    .setChannels(channels)
                    .setSampleRate(sampleRate)
                    .setSamples(samples)
                    .setPcm(pcm)
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Failed to decode " + formatName, e);
        }
    }
}
