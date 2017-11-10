package com.unime.tensorflowproject.interaction;

public class CustomResponse {
    private String response;

    private CustomResponse(String response) {
        this.response = response;
    }

    private String getResponse() {
        return response;
    }

    private void setResponse(String response) {
        this.response = response;
    }
}