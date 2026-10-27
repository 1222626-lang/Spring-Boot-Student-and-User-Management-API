
package com.example.demo.support;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;

@Service
public class StudentSupportService {

    private static final Set<String> ALLOWED_FIELDS =
            Set.of("firstName", "lastName", "email");

    public void validatePatchUpdates(
            Map<String, Object> updates) {

        if (updates == null || updates.isEmpty()) {
            throw new IllegalArgumentException(
                    "At least one field must be provided");
        }

        for (Map.Entry<String, Object> entry
                : updates.entrySet()) {

            String field = entry.getKey();
            Object value = entry.getValue();

            if (!ALLOWED_FIELDS.contains(field)) {
                throw new IllegalArgumentException(
                        "Unknown field: " + field);
            }

            if (!(value instanceof String text)
                    || text.isBlank()) {
                throw new IllegalArgumentException(
                        field + " must be a non-blank string");
            }

            if (field.equals("email")
                    && !text.matches(
                    "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
                throw new IllegalArgumentException(
                        "Email must be valid");
            }
        }
    }
}
