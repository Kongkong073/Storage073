package com.storage073.entity;
import com.aliyun.oss.model.PartETag;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;

import java.io.IOException;

public class PartETagSerializer extends JsonSerializer<PartETag> {

    @Override
    public void serialize(PartETag partETag, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("partNumber", partETag.getPartNumber());
        gen.writeStringField("etag", partETag.getETag());
        gen.writeEndObject();
    }
}
