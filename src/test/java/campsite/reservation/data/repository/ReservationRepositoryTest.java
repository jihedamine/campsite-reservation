package campsite.reservation.data.repository;

import campsite.reservation.data.entity.Reservation;
import campsite.reservation.service.DateResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@SpringBootTest
@Transactional
public class ReservationRepositoryTest {

    @Autowired
    ReservationRepository repository;

    @MockBean
    private DateResolver dateResolver;

    @BeforeEach
    public void setUp() {
        repository.deleteAll();
    }

    @Test
    public void testSaveReservation() {
        final String currentDate = "2020-03-01";
        final String checkInDate = "2020-03-02";
        final String checkOutDate = "2020-03-03";
        final String email = "john.doe@email.com";
        final String fullName = "JohnDoe";
        given(dateResolver.getCurrentDate()).willReturn(LocalDate.parse(currentDate));

        Reservation reservation = Reservation.of(LocalDate.parse(checkInDate),
                LocalDate.parse(checkOutDate), email, fullName, dateResolver);

        repository.save(reservation);

        List<Reservation> reservations = repository.findAll();
        assertThat(reservations).hasSize(1);

        Reservation fetchedReservation = reservations.iterator().next();
        assertThat(fetchedReservation.getCheckInDate()).isEqualTo(checkInDate);
        assertThat(fetchedReservation.getCheckOutDate()).isEqualTo(checkOutDate);
        assertThat(fetchedReservation.getEmail()).isEqualTo(email);
        assertThat(fetchedReservation.getFullName()).isEqualTo(fullName);
    }

    @Test
    public void testUpdateReservation() {
        final String currentDate = "2020-03-01";
        final String checkInDate = "2020-03-02";
        final String checkOutDate = "2020-03-03";
        final String email = "john.doe@email.com";
        final String fullName = "JohnDoe";
        given(dateResolver.getCurrentDate()).willReturn(LocalDate.parse(currentDate));

        Reservation reservation = Reservation.of(LocalDate.parse(checkInDate),
                LocalDate.parse(checkOutDate), email, fullName, dateResolver);

        repository.save(reservation);

        final String newCheckInDate = "2020-03-03";
        final String newCheckOutDate = "2020-03-04";
        final String newEmail = "john.doe2@email.com";
        final String newFullName = "JohnDoe2";

        reservation.setCheckInDate(LocalDate.parse(newCheckInDate));
        reservation.setCheckOutDate(LocalDate.parse(newCheckOutDate));
        reservation.setEmail(newEmail);
        reservation.setFullName(newFullName);

        repository.save(reservation);

        Reservation updatedReservation = repository.getOne(reservation.getId());

        assertThat(updatedReservation.getCheckInDate()).isEqualTo(newCheckInDate);
        assertThat(updatedReservation.getCheckOutDate()).isEqualTo(newCheckOutDate);
        assertThat(updatedReservation.getEmail()).isEqualTo(newEmail);
        assertThat(updatedReservation.getFullName()).isEqualTo(newFullName);
    }
}
