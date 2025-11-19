package org.vrglab.imBoredEngine.tools.vibeloader;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vrglab.imBoredEngine.core.resourceManagment.ResourceEntry;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.SecretKeyFactory;
import java.io.*;
import java.nio.file.*;
import java.security.spec.KeySpec;
import java.util.*;

import static org.vrglab.imBoredEngine.secrets.VibeConstants.*;


public class VibeLoader {
    private static final Logger LOGGER = LogManager.getLogger(VibeLoader.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    private static byte[] dataBlob;
    private static IvParameterSpec ivSpec;
    private static List<ResourceEntry> entries;
    private static SecretKey aesKey;
    private static boolean initialized = false;

    private VibeLoader() {}


    /**
     * Loads and decrypts the .vibe package header + index, but doesn't decrypt files yet.
     */
    public static void loadVibe(Path vibePath) throws IOException {
        if (initialized) return;
        aesKey = deriveAESKey(PASSWORD, SALT);

        try (DataInputStream in = new DataInputStream(Files.newInputStream(vibePath))) {
            byte[] magic = new byte[VIBE_MAGIC.length];
            in.readFully(magic);
            if (!Arrays.equals(magic, VIBE_MAGIC)) {
                throw new IOException("Invalid VIBE file header");
            }

            byte version = in.readByte();
            if (version != VERSION) {
                LOGGER.warn("Version mismatch: file {} vs engine {}", version, VERSION);
            }

            int indexLength = in.readInt();
            byte[] indexBytes = new byte[indexLength];
            in.readFully(indexBytes);
            entries = Arrays.asList(mapper.readValue(indexBytes, ResourceEntry[].class));

            byte[] iv = new byte[16];
            in.readFully(iv);
            ivSpec = new IvParameterSpec(iv);

            dataBlob = in.readAllBytes();
            initialized = true;
            LOGGER.info("VIBE loaded successfully ({} entries)", entries.size());
        }
    }

    /**
     * Returns the decrypted byte array of a given resource inside the loaded .vibe.
     */
    public static byte[] getDecryptedResource(String path) {
        ensureInit();

        for (ResourceEntry entry : entries) {
            if (entry.getName().equals(path)) {
                int offset = entry.getOffset();
                int length = entry.getLength();

                if (offset < 0 || offset + length > dataBlob.length) {
                    throw new IllegalStateException("Invalid entry range for: " + path);
                }

                byte[] encrypted = Arrays.copyOfRange(dataBlob, offset, offset + length);
                return decryptAES(encrypted);
            }
        }

        throw new IllegalArgumentException("Resource not found in VIBE: " + path);
    }


    /**
     * Lists all resources in the loaded .vibe file.
     */
    public static List<ResourceEntry> listEntries() {
        ensureInit();
        return entries;
    }

    private static void ensureInit() {
        if (!initialized)
            throw new IllegalStateException("VibeSystem not initialized. Call loadVibe() first.");
    }

    private static byte[] decryptAES(byte[] data) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, aesKey, ivSpec);
            return cipher.doFinal(data);
        } catch (Exception e) {
            throw new RuntimeException("Failed to decrypt VIBE resource", e);
        }
    }

    private static SecretKey deriveAESKey(String password, String salt) {
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt.getBytes(), 65536, 256);
            SecretKey tmp = factory.generateSecret(spec);
            return new SecretKeySpec(tmp.getEncoded(), "AES");
        } catch (Exception e) {
            throw new RuntimeException("Failed to derive AES key", e);
        }
    }
}
