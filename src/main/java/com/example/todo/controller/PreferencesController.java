package com.example.todo.controller;

import com.example.todo.dto.ViewPreferenceResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.Set;

/**
 * Демонстрирует {@link CookieValue} для чтения куки {@link #VIEW_PREFERENCE_COOKIE};
 * установка куки выполняется через заголовок {@code Set-Cookie} в ответе.
 */
@RestController
@RequestMapping("/api/preferences")
public class PreferencesController {

    public static final String VIEW_PREFERENCE_COOKIE = "viewPreference";
    private static final String DEFAULT_VIEW = "detailed";
    private static final Set<String> ALLOWED_MODES = Set.of("compact", "detailed");

    /**
     * Читает куку через {@link CookieValue @CookieValue} ({@code required = false}, если куки ещё нет).
     * При отсутствии или неверном значении подставляется режим по умолчанию и отдаётся {@code Set-Cookie}.
     */
    @GetMapping("/view")
    public ResponseEntity<ViewPreferenceResponse> getViewPreference(
            @CookieValue(name = VIEW_PREFERENCE_COOKIE, required = false) String viewPreference) {
        String value = viewPreference;
        if (value == null || !ALLOWED_MODES.contains(value)) {
            value = DEFAULT_VIEW;
            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, buildViewCookie(value).toString())
                    .body(new ViewPreferenceResponse(value));
        }
        return ResponseEntity.ok(new ViewPreferenceResponse(value));
    }

    @PostMapping("/view")
    public ResponseEntity<ViewPreferenceResponse> setViewPreference(@RequestParam String mode) {
        if (!ALLOWED_MODES.contains(mode)) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, buildViewCookie(mode).toString())
                .body(new ViewPreferenceResponse(mode));
    }

    private static ResponseCookie buildViewCookie(String value) {
        return ResponseCookie.from(VIEW_PREFERENCE_COOKIE, value)
                .path("/")
                .maxAge(Duration.ofDays(365))
                .httpOnly(false)
                .sameSite("Lax")
                .build();
    }
}
