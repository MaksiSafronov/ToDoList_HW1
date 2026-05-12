package com.example.todo.security;

import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Делегирует BCrypt, добавляя pepper из конфигурации к сырому паролю при encode/matches.
 */
public final class PepperedPasswordEncoder implements PasswordEncoder {

    private final PasswordEncoder delegate;
    private final String pepper;

    public PepperedPasswordEncoder(PasswordEncoder delegate, String pepper) {
        this.delegate = delegate;
        this.pepper = pepper;
    }

    @Override
    public String encode(CharSequence rawPassword) {
        return delegate.encode(withPepper(rawPassword));
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        return delegate.matches(withPepper(rawPassword), encodedPassword);
    }

    @Override
    public boolean upgradeEncoding(String encodedPassword) {
        return delegate.upgradeEncoding(encodedPassword);
    }

    private CharSequence withPepper(CharSequence rawPassword) {
        return rawPassword.toString() + pepper;
    }
}
