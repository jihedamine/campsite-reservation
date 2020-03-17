package campsite.reservation.serialization.types;

import java.time.LocalDate;
import java.util.List;

/**
 * Java type used to serialize a list of dates to JSON format
 */
public class DatesList {
    private List<LocalDate> dates;

    public DatesList(List<LocalDate> dates) {
        this.dates = dates;
    }

    public List<LocalDate> getDates() {
        return dates;
    }
}
