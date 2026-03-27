package com.example.todo.dto;

public class ViewPreferenceResponse {

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
