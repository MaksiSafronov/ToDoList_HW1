package com.example.todo.dto.apiv1;

import java.util.LinkedHashSet;
import java.util.Set;

public class ProfileResponse {

    private String username;
    private Set<String> authorities = new LinkedHashSet<>();

    public ProfileResponse() {
    }

    public ProfileResponse(String username, Set<String> authorities) {
        this.username = username;
        this.authorities = authorities;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Set<String> getAuthorities() {
        return authorities;
    }

    public void setAuthorities(Set<String> authorities) {
        this.authorities = authorities;
    }
}
