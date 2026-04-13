package com.example.todo.service;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class FavoritesService {

    public static final String FAVORITE_TASK_IDS_ATTRIBUTE = "favoriteTaskIds";

    public void addToFavorites(Long taskId, HttpSession session) {
        Set<Long> favoriteTaskIds = getOrCreateFavoriteTaskIds(session);
        favoriteTaskIds.add(taskId);
        session.setAttribute(FAVORITE_TASK_IDS_ATTRIBUTE, favoriteTaskIds);
    }

    public void removeFromFavorites(Long taskId, HttpSession session) {
        Set<Long> favoriteTaskIds = getOrCreateFavoriteTaskIds(session);
        favoriteTaskIds.remove(taskId);
        session.setAttribute(FAVORITE_TASK_IDS_ATTRIBUTE, favoriteTaskIds);
    }

    public List<Long> getFavoriteTaskIds(HttpSession session) {
        return new ArrayList<>(getOrCreateFavoriteTaskIds(session));
    }

    @SuppressWarnings("unchecked")
    private Set<Long> getOrCreateFavoriteTaskIds(HttpSession session) {
        Object existing = session.getAttribute(FAVORITE_TASK_IDS_ATTRIBUTE);
        if (existing instanceof Set<?> existingSet) {
            return (Set<Long>) existingSet;
        }
        Set<Long> favoriteTaskIds = new LinkedHashSet<>();
        session.setAttribute(FAVORITE_TASK_IDS_ATTRIBUTE, favoriteTaskIds);
        return favoriteTaskIds;
    }
}
