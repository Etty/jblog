package com.practice.jblog.formatters;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.logging.Logger;

public class TimeStampDeserializer extends JsonDeserializer<Instant> {
    @Override
    public Instant deserialize(JsonParser p, DeserializationContext ctx)
            throws IOException {
        String str = p.getText();
        try {
            return Instant.parse(str);
        } catch (DateTimeParseException e) {
            Logger logger = Logger.getLogger(getClass().getName());
            logger.info(e.getMessage());
            return null;
        }
    }
}
