package campsite.reservation.data.repository;

import campsite.reservation.data.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Custom JPA Repository to manage reservations
 */
@Repository
public interface ReservationRepository extends JpaRepository<Reservation, String> {

    /**
     * Returns the reservations that have a check-in date after a parameter date
     * @param date date after which the reservations check-in date must be
     * @return the reservations that have a check-in date after a parameter date
     */
    @Query("SELECT r FROM Reservation r WHERE r.checkInDate > ?1")
    List<Reservation> findReservationsFromDate(LocalDate date);
}
