package br.com.swapi.exceptions;

public class InternalServerErrorException extends RuntimeException{
    private String code;
    private String error;
    private String message;

    public InternalServerErrorException(String message, String code, String error) {
        super(message);
        this.code = code;
        this.error = error;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "GenericExceptionDto{" +
                ", code='" + code + '\'' +
                ", error='" + error + '\'' +
                ", message='" + message + '\'' +
                '}';
    }

    public String toJsonString() {
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("{");

        if (code != null) {
            jsonBuilder.append("\"code\":\"").append(code).append("\",");
        }
        if (error != null) {
            jsonBuilder.append("\"error\":\"").append(error).append("\",");
        }
        if (message != null) {
            jsonBuilder.append("\"message\":\"").append(message).append("\",");
        }

        // Remove a vírgula final, se houver
        if (jsonBuilder.charAt(jsonBuilder.length() - 1) == ',') {
            jsonBuilder.deleteCharAt(jsonBuilder.length() - 1);
        }

        jsonBuilder.append("}");

        return jsonBuilder.toString();
    }
}
