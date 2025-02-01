package com.practice.jblog.formatters;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

public class TimeStampSerializer extends JsonSerializer<Instant> {
    @Override
    public void serialize(Instant value, JsonGenerator gen, SerializerProvider provider)
            throws IOException {
        try {
            String s = value.toString();
            gen.writeString(s);
        } catch (DateTimeParseException e) {
            System.err.println(e);
            gen.writeString("");
        }
    }
}
