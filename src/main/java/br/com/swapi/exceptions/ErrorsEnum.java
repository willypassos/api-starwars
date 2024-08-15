package br.com.swapi.exceptions;

public enum ErrorsEnum {

    BAD_REQUEST(400, "BAD_REQUEST", "Client specified an invalid argument, request body or query param"),
    UNAUTHORIZED(401, "UNAUTHORIZED", "Authenticated user has no permission to access the requested resource"),
    NOT_FOUND(404, "NOT_FOUND", "A specified resource is not found"),
    INTERNAL_SERVER_ERROR(500, "INTERNAL_SERVER_ERROR", "Server error"),
    INTERNAL_SERVER_ERROR_NO_DATA(500, "INTERNAL_SERVER_ERROR", "The request made did not bring any data"),
    INTERNAL_SERVER_ERROR_BENEFIT_TYPE(500, "INTERNAL_SERVER_ERROR", "Benefit type not found"),
    FORBIDDEN(403, "FORBIDDEN", "You don't have permission to access / on this server"),
    SERVICE_UNAVAILABLE(503, "SERVICE_UNAVAILABLE", "The service is unavailable"),
    NOT_IMPLEMENTED(501, "NOT_IMPLEMENTED", "Service not implemented");

    private final int statusCode;
    private final String code;
    private final String message;

    ErrorsEnum(int statusCode, String code, String message) {
        this.statusCode = statusCode;
        this.code = code;
        this.message = message;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
