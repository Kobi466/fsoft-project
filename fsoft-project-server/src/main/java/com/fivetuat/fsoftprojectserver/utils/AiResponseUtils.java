package com.fivetuat.fsoftprojectserver.utils;

public final class AiResponseUtils {

    private AiResponseUtils() {
    }

    public static String sanitizeJsonResponse(String rawResponse) {
        if (rawResponse == null) {
            return "";
        }
        String cleaned = rawResponse.trim();
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7);
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3);
        }
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3);
        }
        return cleaned.trim();
    }
}
