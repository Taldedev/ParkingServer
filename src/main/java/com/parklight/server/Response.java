package com.parklight.server;

/**
 * A response returned from the server to a client, serialized to JSON.
 * success tells the client whether the action worked, message is a short
 * human-readable note, and body carries any returned data.
 *
 * @param <T> the type of the response body
 */
public class Response<T> {

    private boolean success;
    private String message;
    private T body;

    public Response() {
    }

    public Response(boolean success, String message, T body) {
        this.success = success;
        this.message = message;
        this.body = body;
    }

    // Builds a success response with a body.
    public static <T> Response<T> ok(T body) {
        return new Response<>(true, "OK", body);
    }

    // Builds a failure response with an error message.
    public static <T> Response<T> error(String message) {
        return new Response<>(false, message, null);
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getBody() {
        return body;
    }

    public void setBody(T body) {
        this.body = body;
    }
}
