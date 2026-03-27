package com.example.todo.service;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class FavoritesServiceTest {

    private final FavoritesService favoritesService = new FavoritesService();

    @Test
    void addToFavorites_storesIdInSession() {
        MockHttpSession session = new MockHttpSession();

        favoritesService.addToFavorites(5L, session);

        @SuppressWarnings("unchecked")
        Set<Long> ids = (Set<Long>) session.getAttribute(FavoritesService.FAVORITE_TASK_IDS_ATTRIBUTE);
        assertThat(ids).containsExactly(5L);
    }

    @Test
    void addToFavorites_multipleIds_preserveOrder() {
        MockHttpSession session = new MockHttpSession();

        favoritesService.addToFavorites(1L, session);
        favoritesService.addToFavorites(2L, session);

        assertThat(favoritesService.getFavoriteTaskIds(session)).containsExactly(1L, 2L);
    }

    @Test
    void removeFromFavorites_removesId() {
        MockHttpSession session = new MockHttpSession();
        favoritesService.addToFavorites(1L, session);
        favoritesService.addToFavorites(2L, session);

        favoritesService.removeFromFavorites(1L, session);

        assertThat(favoritesService.getFavoriteTaskIds(session)).containsExactly(2L);
    }

    @Test
    void getFavoriteTaskIds_emptySession_returnsEmptyList() {
        MockHttpSession session = new MockHttpSession();

        List<Long> ids = favoritesService.getFavoriteTaskIds(session);

        assertThat(ids).isEmpty();
    }
}
