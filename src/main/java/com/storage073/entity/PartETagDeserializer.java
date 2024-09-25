package com.storage073.entity;
import com.aliyun.oss.model.PartETag;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;

import java.io.IOException;

public class PartETagDeserializer extends JsonDeserializer<PartETag> {

    @Override
    public PartETag deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        int partNumber = node.get("partNumber").asInt();
        String etag = node.get("etag").asText();
        return new PartETag(partNumber, etag);
    }
}