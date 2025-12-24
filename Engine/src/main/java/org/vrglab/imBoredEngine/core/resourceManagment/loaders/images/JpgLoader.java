package org.vrglab.imBoredEngine.core.resourceManagment.loaders.images;

import org.vrglab.imBoredEngine.core.resourceManagment.Annotations.ResourceLoaders;
import org.vrglab.imBoredEngine.core.resourceManagment.interfaces.ResourceLoader;
import org.vrglab.imBoredEngine.core.resourceManagment.resourceTypes.Image;

@ResourceLoaders
public class JpgLoader extends ResourceLoader<Image> {
    @Override
    public String getResourceRegex() {
        return "*.jpg";
    }

    @Override
    public Image load(byte[] fileContent) {
        return ImageCreator.makeImage(fileContent);
    }
}
