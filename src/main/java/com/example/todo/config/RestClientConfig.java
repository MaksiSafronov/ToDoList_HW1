package com.example.todo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
public class RestClientConfig {

    @Bean(name = "externalTasksRestClient")
    public RestClient externalTasksRestClient(
            @Value("${app.external-tasks.base-url}") String baseUrl,
            @Value("${app.external-tasks.connect-timeout-ms}") int connectTimeoutMs,
            @Value("${app.external-tasks.read-timeout-ms}") int readTimeoutMs,
            @Value("${app.external-tasks.user-agent}") String userAgent) {

        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(connectTimeoutMs))
                .build();

        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofMillis(readTimeoutMs));

        return RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(org.springframework.http.HttpHeaders.USER_AGENT, userAgent)
                .requestFactory(requestFactory)
                .build();
    }
}
