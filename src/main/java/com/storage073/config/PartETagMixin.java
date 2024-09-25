package com.storage073.config;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

// Mixin 类仅用于定义序列化/反序列化行为
public abstract class PartETagMixin {

    @JsonCreator
    public PartETagMixin(@JsonProperty("partNumber") int partNumber,
                         @JsonProperty("eTag") String eTag,
                         @JsonProperty("partSize") long partSize,
                         @JsonProperty("partCRC") Long partCRC) {
        // Mixin 类不需要实际的实现，只定义序列化/反序列化行为
    }
}
