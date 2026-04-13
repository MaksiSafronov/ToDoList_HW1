package com.example.todo.controller;

import com.example.todo.dto.ErrorResponse;
import com.example.todo.dto.ViewPreferenceResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Preferences", description = "Пользовательские настройки (куки)")
@RestController
@RequestMapping("/api/preferences")
public class PreferencesController {

    public static final String VIEW_PREFERENCE_COOKIE = "viewPreference";
    private static final String DEFAULT_VIEW = "detailed";
    private static final Set<String> ALLOWED_MODES = Set.of("compact", "detailed");

    @Operation(summary = "Прочитать режим отображения списка", description = "Кука viewPreference; при отсутствии выставляется значение по умолчанию")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Текущий или значение по умолчанию",
                    content = @Content(schema = @Schema(implementation = ViewPreferenceResponse.class)))
    })
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

    @Operation(summary = "Установить режим отображения")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Кука обновлена",
                    content = @Content(schema = @Schema(implementation = ViewPreferenceResponse.class))),
            @ApiResponse(responseCode = "400", description = "Неверный mode",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/view")
    public ResponseEntity<ViewPreferenceResponse> setViewPreference(
            @Parameter(description = "compact или detailed", required = true, example = "compact")
            @RequestParam String mode) {
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
