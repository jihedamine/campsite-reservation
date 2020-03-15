package campsite.reservation.data.entity;

import javax.persistence.Entity;
import javax.persistence.Id;

import java.time.ZonedDateTime;

@Entity
public class Reservation {

    @Id
    private final String id;

    private final ZonedDateTime startDate;

    // Needed by Hibernate
    private Reservation() {
        this.id = null;
        this.startDate = null;
    }

    public Reservation(String id, ZonedDateTime startDate) {
        this.id = id;
        this.startDate = startDate;
    }

    public String getId() {
        return id;
    }

    public ZonedDateTime getStartDate() {
        return startDate;
    }
}

