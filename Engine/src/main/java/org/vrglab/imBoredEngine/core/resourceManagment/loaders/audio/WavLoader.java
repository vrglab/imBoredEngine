package org.vrglab.imBoredEngine.core.resourceManagment.loaders.audio;

import org.vrglab.imBoredEngine.core.resourceManagment.Annotations.ResourceLoaders;
import org.vrglab.imBoredEngine.core.resourceManagment.interfaces.ResourceLoader;
import org.vrglab.imBoredEngine.core.resourceManagment.resourceTypes.Audio;

@ResourceLoaders
public class WavLoader extends ResourceLoader<Audio> {

    @Override
    public String getResourceRegex() {
        return "*.wav";
    }

    @Override
    public Audio load(byte[] fileContent) {
        return Utils.buildWithInternalJavaSystem(fileContent, "wav");
    }
}
