package io.sci.citizen.api;

import java.io.Serializable;

public class Response implements Serializable {
    private Object data;
    private String message;

    public Response(Object data, String message) {
        this.data = data;
        this.message = message;
    }

    public Response(String message) {
        this.message = message;
    }

    public Response(Object data) {
        this.data = data;
    }

    public Response() {
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
