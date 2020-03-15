package campsite.reservation.data.repository;

import campsite.reservation.data.entity.Reservation;
import campsite.reservation.data.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class ReservationRepositoryTest {

    @Autowired
    ReservationRepository repository;

    @BeforeEach
    public void setUp() {
        repository.deleteAll();
    }

    @Test
    public void testSaveReservation() {
        final String testId = "123";
        final ZonedDateTime startDate = ZonedDateTime.of(2020, 3, 10,
                12, 0, 0, 0, TimeZone.getTimeZone("EST").toZoneId());
        Reservation reservation = new Reservation(testId, startDate);

        repository.save(reservation);

        List<Reservation> reservations = repository.findAll();
        assertThat(reservations).hasSize(1);

        Reservation fetchedReservation = reservations.iterator().next();
        assertThat(fetchedReservation.getId()).isEqualTo(testId);
        assertThat(fetchedReservation.getStartDate()).isEqualTo(startDate);
    }
}
