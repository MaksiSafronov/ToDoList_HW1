package com.example.todo.logging;

public final class JwtMaskingUtils {

    private static final int VISIBLE_EDGE = 6;

    private JwtMaskingUtils() {
    }

    public static String maskAuthorizationHeader(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            return "<none>";
        }
        if (!authorizationHeader.startsWith("Bearer ")) {
            return "<non-bearer>";
        }
        String token = authorizationHeader.substring("Bearer ".length()).trim();
        return "Bearer " + maskToken(token);
    }

    public static String maskToken(String token) {
        if (token == null || token.isBlank()) {
            return "<empty>";
        }
        if (token.length() <= VISIBLE_EDGE * 2) {
            return "***";
        }
        String prefix = token.substring(0, VISIBLE_EDGE);
        String suffix = token.substring(token.length() - VISIBLE_EDGE);
        return prefix + "..." + suffix;
    }
}
