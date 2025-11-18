package org.vrglab.imBoredEngine.core.resourceManagment.loaders.images;

import org.lwjgl.bgfx.BGFX;
import org.lwjgl.bgfx.BGFXMemory;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.vrglab.imBoredEngine.core.resourceManagment.resourceTypes.Image;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class ImageUtils {

    public static Image makeImage(byte[] fileContent){
        ByteBuffer imageBuffer = MemoryUtil.memAlloc(fileContent.length);
        imageBuffer.put(fileContent).flip();
        int w = 0;
        int h = 0;
        int ch = 0;
        ByteBuffer _image = null;

        int textureHandle;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer width = stack.mallocInt(1);
            IntBuffer height = stack.mallocInt(1);
            IntBuffer channels = stack.mallocInt(1);


            ByteBuffer image = STBImage.stbi_load_from_memory(imageBuffer, width, height, channels, 4);
            if (image == null) {
                throw new RuntimeException("Failed to load image: " + STBImage.stbi_failure_reason());
            }

            _image = image;

            w = width.get(0);
            h = height.get(0);
            ch = channels.get(0);


            BGFXMemory mem = BGFX.bgfx_make_ref_release(
                    image,
                    (ptr, userData) -> STBImage.stbi_image_free(image),
                    0L
            );


            textureHandle = BGFX.bgfx_create_texture_2d(
                    (short) w,
                    (short) h,
                    false,
                    1,
                    BGFX.BGFX_TEXTURE_FORMAT_RGBA8,
                    BGFX.BGFX_SAMPLER_U_CLAMP | BGFX.BGFX_SAMPLER_V_CLAMP,
                    mem
            );
        }
        MemoryUtil.memFree(imageBuffer);
        return Image.Builder.create().handle(textureHandle).data(_image).width(w).height(h).channels(ch).build();
    }
}
