package com.adnane.moulcyber.api.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

final class JsonTestValues {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private JsonTestValues() {
    }

    static String tokenFrom(String json) throws JsonProcessingException {
        return OBJECT_MAPPER.readTree(json).get("token").asText();
    }
}
