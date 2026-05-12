package com.example.todo.client;

import com.example.todo.dto.external.ExternalTaskCreateRequest;
import com.example.todo.dto.external.ExternalTaskResponse;
import com.example.todo.exception.ExternalApiException;
import com.example.todo.exception.TaskNotFoundException;
import com.example.todo.service.tasks.CreatedTask;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
public class ExternalTasksClient {

    private static final Logger log = LoggerFactory.getLogger(ExternalTasksClient.class);
    private static final int MAX_LOG_BODY = 512;

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public ExternalTasksClient(
            @Qualifier("externalTasksRestClient") RestClient restClient,
            ObjectMapper objectMapper) {
        this.restClient = restClient;
        this.objectMapper = objectMapper;
    }

    public CreatedTask createTask(ExternalTaskCreateRequest body) {
        try {
            ResponseEntity<ExternalTaskResponse> entity = restClient.post()
                    .uri("/tasks")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toEntity(ExternalTaskResponse.class);
            if (!entity.getStatusCode().equals(HttpStatus.CREATED)) {
                throw new ExternalApiException(entity.getStatusCode(), "POST /tasks: expected 201 Created");
            }
            URI location = entity.getHeaders().getLocation();
            ExternalTaskResponse task = entity.getBody();
            if (task == null && location != null) {
                task = fetchTaskByUri(location);
            }
            if (task == null) {
                throw new ExternalApiException(HttpStatus.BAD_GATEWAY, "POST /tasks: empty body and no Location");
            }
            return new CreatedTask(task, location);
        } catch (RestClientResponseException ex) {
            throw translate("POST /tasks", ex, null);
        } catch (RestClientException ex) {
            throw new ExternalApiException(HttpStatus.BAD_GATEWAY, "POST /tasks: " + ex.getMessage());
        }
    }

    public ExternalTaskResponse getTask(long id) {
        try {
            return restClient.get()
                    .uri("/tasks/{id}", id)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(ExternalTaskResponse.class);
        } catch (RestClientResponseException ex) {
            throw translate("GET /tasks/" + id, ex, id);
        } catch (RestClientException ex) {
            throw new ExternalApiException(HttpStatus.BAD_GATEWAY, "GET /tasks/" + id + ": " + ex.getMessage());
        }
    }

    public List<ExternalTaskResponse> listTasks(Boolean completed, Integer limit) {
        try {
            List<ExternalTaskResponse> list = restClient.get()
                    .uri(uriBuilder -> listTasksUri(uriBuilder, completed, limit))
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });
            return list != null ? list : Collections.emptyList();
        } catch (RestClientResponseException ex) {
            throw translate("GET /tasks", ex, null);
        } catch (RestClientException ex) {
            throw new ExternalApiException(HttpStatus.BAD_GATEWAY, "GET /tasks: " + ex.getMessage());
        }
    }

    private static URI listTasksUri(UriBuilder uriBuilder, Boolean completed, Integer limit) {
        UriBuilder b = uriBuilder.path("/tasks");
        if (completed != null) {
            b.queryParam("completed", completed);
        }
        if (limit != null) {
            b.queryParam("limit", limit);
        }
        return b.build();
    }

    public void deleteTask(long id) {
        try {
            ResponseEntity<Void> res = restClient.delete()
                    .uri("/tasks/{id}", id)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .toBodilessEntity();
            if (!res.getStatusCode().equals(HttpStatus.NO_CONTENT)) {
                throw new ExternalApiException(res.getStatusCode(), "DELETE /tasks/" + id + ": expected 204 No Content");
            }
        } catch (RestClientResponseException ex) {
            throw translate("DELETE /tasks/" + id, ex, id);
        } catch (RestClientException ex) {
            throw new ExternalApiException(HttpStatus.BAD_GATEWAY, "DELETE /tasks/" + id + ": " + ex.getMessage());
        }
    }

    private ExternalTaskResponse fetchTaskByUri(URI location) {
        try {
            return restClient.get()
                    .uri(location)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(ExternalTaskResponse.class);
        } catch (RestClientResponseException ex) {
            Long id = parseIdFromTasksPath(location);
            throw translate("GET " + location, ex, id);
        } catch (RestClientException ex) {
            throw new ExternalApiException(HttpStatus.BAD_GATEWAY, "GET " + location + ": " + ex.getMessage());
        }
    }

    private static Long parseIdFromTasksPath(URI location) {
        String path = location.getPath();
        if (path == null) {
            return null;
        }
        int idx = path.lastIndexOf('/');
        if (idx < 0 || idx >= path.length() - 1) {
            return null;
        }
        try {
            return Long.parseLong(path.substring(idx + 1));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private RuntimeException translate(String operation, RestClientResponseException ex, Long taskId) {
        HttpStatusCode code = ex.getStatusCode();
        String body = responseBody(ex);
        logNonJsonBodyIfNeeded(operation, ex, body);

        if (code.value() == 404) {
            String detail = parseProblemDetail(body);
            if (taskId != null) {
                return new TaskNotFoundException(taskId, detail);
            }
            return new ExternalApiException(code, operation + ": " + (detail != null ? detail : "Not found"));
        }
        if (code.is5xxServerError()) {
            return new ExternalApiException(code, operation + ": upstream " + code.value());
        }
        return new ExternalApiException(code, operation + ": HTTP " + code.value() + " " + truncate(body, 200));
    }

    private void logNonJsonBodyIfNeeded(String operation, RestClientResponseException ex, String body) {
        MediaType ct = Optional.ofNullable(ex.getResponseHeaders())
                .map(headers -> headers.getContentType())
                .orElse(null);
        if (ct != null && !ct.isCompatibleWith(MediaType.APPLICATION_JSON)) {
            log.warn(
                    "{}: upstream returned unexpected content-type={} body(truncated)={}",
                    operation,
                    ct,
                    truncate(body, MAX_LOG_BODY)
            );
        }
    }

    private String responseBody(RestClientResponseException ex) {
        try {
            return ex.getResponseBodyAsString(StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "";
        }
    }

    private String parseProblemDetail(String body) {
        if (body == null || body.isBlank()) {
            return null;
        }
        try {
            JsonNode n = objectMapper.readTree(body);
            JsonNode detail = n.get("detail");
            if (detail != null && detail.isTextual()) {
                return detail.asText();
            }
        } catch (Exception ignored) {
            // not JSON ProblemDetails
        }
        return null;
    }

    private static String truncate(String s, int max) {
        if (s == null) {
            return "";
        }
        return s.length() <= max ? s : s.substring(0, max) + "…";
    }
}
