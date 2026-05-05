package tn.star.Pfe.entity.inscription;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import tn.star.Pfe.dto.inscription.GuestDTO;

import java.util.ArrayList;
import java.util.List;

@Converter
public class GuestListConverter implements AttributeConverter<List<GuestDTO>, String> {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<GuestDTO> guests) {
        if (guests == null || guests.isEmpty()) return "[]";
        try {
            return MAPPER.writeValueAsString(guests);
        } catch (Exception e) {
            return "[]";
        }
    }

    @Override
    public List<GuestDTO> convertToEntityAttribute(String json) {
        if (json == null || json.isBlank()) return new ArrayList<>();
        try {
            return MAPPER.readValue(json, new TypeReference<List<GuestDTO>>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}
