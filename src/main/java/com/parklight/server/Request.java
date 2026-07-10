package com.parklight.server;

import java.util.Map;

/**
 * A request sent from a client to the server, parsed from JSON.
 * headers carries metadata such as the "action" that selects the handler.
 * body carries the payload (for example the fields of a data model).
 *
 * @param <T> the type of the request body
 */
public class Request<T> {

    private Map<String, String> headers;
    private T body;

    public Request() {
    }

    public Request(Map<String, String> headers, T body) {
        this.headers = headers;
        this.body = body;
    }

    // Builds a request with a single "action" header and a body.
    public Request(String action, T body) {
        this.headers = new java.util.HashMap<>();
        this.headers.put("action", action);
        this.body = body;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public T getBody() {
        return body;
    }

    public void setBody(T body) {
        this.body = body;
    }

    // Convenience: reads a single header value, or null if absent.
    public String getHeader(String key) {
        return headers == null ? null : headers.get(key);
    }
}
