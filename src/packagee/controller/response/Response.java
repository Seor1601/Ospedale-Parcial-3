package packagee.controller.response;

public class Response {

    private final StatusCode statusCode;
    private final boolean success;
    private final String message;
    private final String data;

    public Response(StatusCode statusCode, boolean success, String message, String data) {
        this.statusCode = statusCode;
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public StatusCode getStatusCode() {
        return statusCode;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getData() {
        return data;
    }

    public static Response ok(String message) {
        return new Response(StatusCode.OK, true, message, null);
    }

    public static Response ok(String message, String data) {
        return new Response(StatusCode.OK, true, message, data);
    }

    public static Response created(String message, String data) {
        return new Response(StatusCode.CREATED, true, message, data);
    }

    public static Response badRequest(String message) {
        return new Response(StatusCode.BAD_REQUEST, false, message, null);
    }

    public static Response notFound(String message) {
        return new Response(StatusCode.NOT_FOUND, false, message, null);
    }

    public static Response conflict(String message) {
        return new Response(StatusCode.CONFLICT, false, message, null);
    }

    public static Response unauthorized(String message) {
        return new Response(StatusCode.UNAUTHORIZED, false, message, null);
    }

    public static Response forbidden(String message) {
        return new Response(StatusCode.FORBIDDEN, false, message, null);
    }

    public static Response internalError(String message) {
        return new Response(StatusCode.INTERNAL_ERROR, false, message, null);
    }
}
