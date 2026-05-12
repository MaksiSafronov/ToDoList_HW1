package com.example.todo.config;

import com.example.todo.security.JwtAuthFilter;
import com.example.todo.security.JwtUtils;
import com.example.todo.security.PepperedPasswordEncoder;
import com.example.todo.security.RestAccessDeniedHandler;
import com.example.todo.security.RestAuthenticationEntryPoint;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder(@org.springframework.beans.factory.annotation.Value("${app.security.password-pepper}") String pepper) {
        return new PepperedPasswordEncoder(new BCryptPasswordEncoder(), pepper);
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        UserDetails user = User.withUsername("user")
                .password(passwordEncoder.encode("password"))
                .authorities("ROLE_USER")
                .build();
        UserDetails reader = User.withUsername("reader")
                .password(passwordEncoder.encode("password"))
                .authorities("ROLE_USER", "READ_PRIVILEGE")
                .build();
        return new InMemoryUserDetailsManager(user, reader);
    }

    @Bean
    public RequestMatcher permitAllRequestMatcher() {
        return new OrRequestMatcher(
                new AntPathRequestMatcher("/api/v1/auth/login", HttpMethod.POST.name()),
                new AntPathRequestMatcher("/external/**"),
                new AntPathRequestMatcher("/actuator/health"),
                new AntPathRequestMatcher("/actuator/health/**"),
                new AntPathRequestMatcher("/actuator/metrics"),
                new AntPathRequestMatcher("/actuator/metrics/**"),
                new AntPathRequestMatcher("/v3/api-docs/**"),
                new AntPathRequestMatcher("/swagger-ui/**"),
                new AntPathRequestMatcher("/swagger-ui.html"),
                new AntPathRequestMatcher("/api/tasks/**"),
                new AntPathRequestMatcher("/api/attachments/**"),
                new AntPathRequestMatcher("/api/favorites/**"),
                new AntPathRequestMatcher("/api/preferences/**")
        );
    }

    @Bean
    public JwtAuthFilter jwtAuthFilter(JwtUtils jwtUtils, ObjectMapper objectMapper, RequestMatcher permitAllRequestMatcher) {
        return new JwtAuthFilter(jwtUtils, objectMapper, permitAllRequestMatcher);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthFilter jwtAuthFilter,
            RestAuthenticationEntryPoint authenticationEntryPoint,
            RestAccessDeniedHandler accessDeniedHandler) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                        .requestMatchers("/external/**").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                        .requestMatchers("/actuator/metrics", "/actuator/metrics/**").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/api/tasks/**", "/api/attachments/**", "/api/favorites/**", "/api/preferences/**").permitAll()
                        .requestMatchers("/api/v1/profile").hasRole("USER")
                        .requestMatchers("/api/v1/docs").hasAuthority("READ_PRIVILEGE")
                        .requestMatchers(HttpMethod.GET, "/api/v1/tasks", "/api/v1/tasks/**").hasRole("USER")
                        .requestMatchers(HttpMethod.POST, "/api/v1/tasks", "/api/v1/tasks/**").hasRole("USER")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/tasks", "/api/v1/tasks/**").hasRole("USER")
                        .requestMatchers("/api/v1/**").denyAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable);

        http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
