package campsite.reservation.serialization.types;

/**
 *  Java type used to serialize a reservation id to JSON format
 */
public class ReservationId {
    private String reservationId;

    public ReservationId(String reservationId) {
        this.reservationId = reservationId;
    }

    public String getReservationId() {
        return reservationId;
    }
}
