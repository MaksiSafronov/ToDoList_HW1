package com.example.todo.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

@Converter
public class StringSetJsonConverter implements AttributeConverter<Set<String>, String> {

	private static final ObjectMapper MAPPER = new ObjectMapper();
	private static final TypeReference<LinkedHashSet<String>> TYPE = new TypeReference<>() {
	};

	@Override
	public String convertToDatabaseColumn(Set<String> attribute) {
		if (attribute == null || attribute.isEmpty()) {
			return null;
		}
		try {
			return MAPPER.writeValueAsString(attribute);
		} catch (JsonProcessingException e) {
			throw new IllegalArgumentException("Cannot serialize tags to JSON", e);
		}
	}

	@Override
	public Set<String> convertToEntityAttribute(String dbData) {
		if (dbData == null || dbData.isBlank()) {
			return null;
		}
		try {
			LinkedHashSet<String> parsed = MAPPER.readValue(dbData, TYPE);
			return parsed.isEmpty() ? Collections.emptySet() : parsed;
		} catch (JsonProcessingException e) {
			throw new IllegalArgumentException("Cannot deserialize tags from JSON", e);
		}
	}
}
