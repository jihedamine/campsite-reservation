package campsite.reservation.service;

import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DateResolver {

    public LocalDate getCurrentDate() {
        return LocalDate.now();
    }

}
