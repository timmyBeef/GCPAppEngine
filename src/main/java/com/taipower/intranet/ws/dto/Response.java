package com.taipower.intranet.ws.dto;

import java.util.List;

public class Response {
    List<entity> response;

    public List<entity> getResponse() {
        return response;
    }

    public void setResponse(List<entity> response) {
        this.response = response;
    }
}

class entity {
    String content;
    String code;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}