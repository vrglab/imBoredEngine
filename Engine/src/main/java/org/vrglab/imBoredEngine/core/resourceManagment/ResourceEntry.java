package org.vrglab.imBoredEngine.core.resourceManagment;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ResourceEntry {
    private String name, type, sha256;
    private int offset,length;

    private byte[] rawData;

    @JsonCreator
    public ResourceEntry(
            @JsonProperty("name") String name,
            @JsonProperty("type") String type,
            @JsonProperty("offset") int offset,
            @JsonProperty("length") int length,
            @JsonProperty("sha256") String sha256
    ) {
        this.name = name;
        this.type = type;
        this.offset = offset;
        this.length = length;
        this.sha256 = sha256;
    }

    public int getLength() {
        return length;
    }

    public String getName() {
        return name;
    }

    public int getOffset() {
        return offset;
    }

    public String getSha256() {
        return sha256;
    }

    public String getType() {
        return type;
    }

    public byte[] getRawData() {
        return rawData;
    }

    public void setRawData(byte[] rawData) {
        this.rawData = rawData;
    }
}
