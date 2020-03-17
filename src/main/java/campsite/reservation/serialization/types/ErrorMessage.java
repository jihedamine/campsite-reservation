package campsite.reservation.serialization.types;

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
