package com.example.todo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ViewPreferenceResponse", description = "Режим отображения списка задач")
public class ViewPreferenceResponse {

    @Schema(description = "Режим: compact или detailed", example = "detailed", allowableValues = {"compact", "detailed"})
    private String view;

    public ViewPreferenceResponse() {
    }

    public ViewPreferenceResponse(String view) {
        this.view = view;
    }

    public String getView() {
        return view;
    }

    public void setView(String view) {
        this.view = view;
    }
}
