package br.com.swapi.model.enuns;

public enum HttpStatus {
    CREATED(201, "Created"),
    OK(200, "OK");

    private final int code;
    private final String message;

    HttpStatus(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }
    public String getMessage() {
        return message;
    }
}
