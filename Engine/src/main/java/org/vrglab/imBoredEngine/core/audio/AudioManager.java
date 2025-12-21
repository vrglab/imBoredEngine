package org.vrglab.imBoredEngine.core.audio;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.openal.*;
import org.vrglab.imBoredEngine.core.initializer.ApplicationInitializer;
import org.vrglab.imBoredEngine.core.initializer.annotations.CalledDuringInit;
import org.vrglab.imBoredEngine.core.initializer.annotations.CalledDuringShutdown;
import org.vrglab.imBoredEngine.core.resourceManagment.resourceTypes.Audio;

import java.nio.ByteBuffer;

import static org.vrglab.imBoredEngine.core.utils.MemoryUtils.*;


public class AudioManager {
    private static final Logger LOGGER = LogManager.getLogger(AudioManager.class);

    private static long device, context;
    private static ALCapabilities caps;


    public static int prepareSource(Audio audio) {
        int source = AL10.alGenSources();
        int buffer = AL10.alGenBuffers();

        AL10.alBufferData(buffer, audio.getALFormat(), audio.getPcm(), audio.getSampleRate());

        AL10.alSourcei(source, AL10.AL_BUFFER, buffer);

        AL10.alSourcef(source, AL10.AL_GAIN, 1.0f);
        AL10.alSourcef(source, AL10.AL_PITCH, 1.0f);
        AL10.alSource3f(source, AL10.AL_POSITION, 0f, 0f, 0f);
        return source;
    }

    @CalledDuringInit(priority = 6)
    private static void init() {
        LOGGER.info("Starting Audio Manager");
        device = ALC10.alcOpenDevice(B_NULL);
        if(device == NULL) {
            throw new RuntimeException("Failed to open the default audio device");
        }

        context = ALC10.alcCreateContext(device, I_NULL);
        if(context == NULL) {
            throw new RuntimeException("Failed to create an OpenAL context");
        }
        ALC10.alcMakeContextCurrent(context);

        ALCCapabilities cCaps = ALC.createCapabilities(device);
        caps = AL.createCapabilities(cCaps);



        String deviceName = ALC10.alcGetString(device, ALC10.ALC_DEVICE_SPECIFIER);
        String defaultDeviceName = ALC10.alcGetString(0, ALC10.ALC_DEFAULT_DEVICE_SPECIFIER);
        String vendor = AL10.alGetString(AL10.AL_VENDOR);
        String renderer = AL10.alGetString(AL10.AL_RENDERER);
        String version = AL10.alGetString(AL10.AL_VERSION);
        String extensions = AL10.alGetString(AL10.AL_EXTENSIONS);

        LOGGER.info("=== OpenAL Info ===");
        LOGGER.info("Device: {}", deviceName);
        LOGGER.info("Default Device: {}", defaultDeviceName);
        LOGGER.info("Vendor: {}", vendor);
        LOGGER.info("Renderer: {}", renderer);
        LOGGER.info("Version: {}", version);
        LOGGER.info("Extensions: {}", extensions);
        LOGGER.info("===============================");
    }

    @CalledDuringShutdown(priority = 3)
    private static void shutdown() {
        ALC10.alcCloseDevice(device);
        ALC10.alcDestroyContext(context);
        device = context = NULL;
    }
}
