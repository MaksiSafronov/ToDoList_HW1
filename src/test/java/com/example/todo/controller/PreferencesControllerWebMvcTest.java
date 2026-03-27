package com.example.todo.controller;

import com.example.todo.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.servlet.http.Cookie;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PreferencesController.class)
@Import(GlobalExceptionHandler.class)
class PreferencesControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private Environment environment;

    @Test
    void getViewPreference_noCookie_returnsDefaultAndSetsCookie() throws Exception {
        mockMvc.perform(get("/api/preferences/view"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.view").value("detailed"))
                .andExpect(header().string("Set-Cookie", containsString("viewPreference=detailed")));
    }

    @Test
    void getViewPreference_validCookie_returnsSameValue() throws Exception {
        mockMvc.perform(get("/api/preferences/view")
                        .cookie(new Cookie(PreferencesController.VIEW_PREFERENCE_COOKIE, "compact")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.view").value("compact"));
    }

    @Test
    void getViewPreference_invalidCookieValue_returnsDefault() throws Exception {
        mockMvc.perform(get("/api/preferences/view")
                        .cookie(new Cookie(PreferencesController.VIEW_PREFERENCE_COOKIE, "unknown")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.view").value("detailed"));
    }

    @Test
    void setViewPreference_positive_returnsOkAndSetsCookie() throws Exception {
        mockMvc.perform(post("/api/preferences/view")
                        .param("mode", "compact")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.view").value("compact"))
                .andExpect(header().string("Set-Cookie", containsString("viewPreference=compact")));
    }

    @Test
    void setViewPreference_invalidMode_returns400() throws Exception {
        mockMvc.perform(post("/api/preferences/view")
                        .param("mode", "wide"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void setViewPreference_missingMode_returns400() throws Exception {
        mockMvc.perform(post("/api/preferences/view"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}
