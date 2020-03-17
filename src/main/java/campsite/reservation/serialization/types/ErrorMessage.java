package campsite.reservation.serialization.types;

/**
 * Java type used to serialize an error message to JSON format
 */
public class ErrorMessage {
    private Integer statusCode;
    private String message;

    public ErrorMessage(Integer statusCode, String message) {
        this.statusCode = statusCode;
        this.message = message;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public String getMessage() {
        return message;
    }
}
