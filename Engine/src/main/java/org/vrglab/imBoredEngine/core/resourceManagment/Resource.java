package org.vrglab.imBoredEngine.core.resourceManagment;

import java.io.File;

public class Resource<T> {
    private String resourceName;
    private File resourceFile;
    private byte[] rawFileData;
    private T resourceData;

    public Resource(byte[] rawFileData, T resourceData, File resourceFile, String resourceName) {
        this.rawFileData = rawFileData;
        this.resourceData = resourceData;
        this.resourceFile = resourceFile;
        this.resourceName = resourceName;
    }

    public byte[] getRawFileData() {
        return rawFileData;
    }

    public T getResourceData() {
        return resourceData;
    }

    public File getResourceFile() {
        return resourceFile;
    }

    public String getResourceName() {
        return resourceName;
    }
}
