package campsite.reservation.service;

import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Used in unit tests to inject a specific date as the current date
 */
@Component
public class DateResolver {

    public LocalDate getCurrentDate() {
        return LocalDate.now();
    }

}
