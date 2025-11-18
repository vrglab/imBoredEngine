package org.vrglab.imBoredEngine.core.resourceManagment.interfaces;

public abstract class ResourceLoader<T> {

    public abstract String getResourceRegex();
    public abstract T load(byte[] fileContent);
}
