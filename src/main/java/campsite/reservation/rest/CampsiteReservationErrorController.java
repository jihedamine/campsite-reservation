package campsite.reservation.rest;

import campsite.reservation.serialization.types.ErrorMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.WebRequest;

import javax.servlet.http.HttpServletResponse;

/**
 * Rest Controller that intercepts exceptions and wraps them in ErrorMessage objects to return to the called
 */
@RestController
public class CampsiteReservationErrorController implements ErrorController {
    private final static Logger logger = LoggerFactory.getLogger(CampsiteReservationErrorController.class.getName());

    private static final String ERROR_PATH = "error";

    @RequestMapping(value = ERROR_PATH, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ErrorMessage error(WebRequest request, HttpServletResponse response) {
        Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code", RequestAttributes.SCOPE_REQUEST);
        Exception exception = (Exception) request.getAttribute("javax.servlet.error.exception", RequestAttributes.SCOPE_REQUEST);

        String errorMessage = getErrorMessage(exception);

        logger.error("Failed to process request. Returning status code {} and error message: {}", statusCode, errorMessage, exception);
        return new ErrorMessage(statusCode, errorMessage);
    }

    /**
     * Returns message string of the root exception
     * @param exception Exception to parse
     * @return message string of the root exception
     */
    private String getErrorMessage(Exception exception) {
        if (exception == null) {
            return "N/A";
        } else if (exception.getCause() != null) {
            return exception.getCause().getMessage();
        } else {
            return exception.getMessage();
        }
    }

    @Override
    public String getErrorPath() {
        return ERROR_PATH;
    }
}
