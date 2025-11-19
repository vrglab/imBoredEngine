package org.vrglab.imBoredEngine.core.resourceManagment.loaders.images;

import org.vrglab.imBoredEngine.core.resourceManagment.Annotations.ResourceLoaders;
import org.vrglab.imBoredEngine.core.resourceManagment.interfaces.ResourceLoader;
import org.vrglab.imBoredEngine.core.resourceManagment.resourceTypes.Image;

@ResourceLoaders
public class PngLoader extends ResourceLoader<Image> {
    @Override
    public String getResourceRegex() {
        return "*.png";
    }

    @Override
    public Image load(byte[] fileContent) {
        return ImageCreator.makeImage(fileContent);
    }
}
