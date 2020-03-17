package campsite.reservation.data.entity;

import campsite.reservation.service.DateResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class ReservationTest {

    private DateResolver dateResolver;

    @BeforeEach
    public void setUp() {
        dateResolver = mock(DateResolver.class);
    }

    @Test
    public void testCreateSuccessfulReservation() {
        final String currentDate = "2020-03-01";
        doReturn(LocalDate.parse(currentDate)).when(dateResolver).getCurrentDate();

        final String checkInDate = "2020-03-05";
        final String checkOutDate = "2020-03-08";
        final String email = "john.doe@email.com";
        final String fullName = "JohnDoe";

        Reservation reservation = Reservation.of(LocalDate.parse(checkInDate),
                LocalDate.parse(checkOutDate), email, fullName, dateResolver);

        assertThat(reservation.getCheckInDate()).isEqualTo(checkInDate);
        assertThat(reservation.getCheckOutDate()).isEqualTo(checkOutDate);
        assertThat(reservation.getEmail()).isEqualTo(email);
        assertThat(reservation.getFullName()).isEqualTo(fullName);
    }

    @Test
    public void testCannotCreateReservationWithNullParameters() {
        final String currentDate = "2020-03-01";
        doReturn(LocalDate.parse(currentDate)).when(dateResolver).getCurrentDate();

        assertThatThrownBy(() ->
                Reservation.of(null, null, null, null, dateResolver)
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot create reservation with missing required field(s)");
    }

    @Test
    public void testCannotCreateReservationWithEmptyEmail() {
        final String currentDate = "2020-03-01";
        doReturn(LocalDate.parse(currentDate)).when(dateResolver).getCurrentDate();

        final String checkInDate = "2020-03-05";
        final String checkOutDate = "2020-03-08";
        final String email = " ";
        final String fullName = "JohnDoe";

        assertThatThrownBy(() ->
                Reservation.of(LocalDate.parse(checkInDate), LocalDate.parse(checkOutDate),
                        email, fullName, dateResolver)
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot create reservation with missing required field(s)");
    }

    @Test
    public void testCannotCreateReservationWithEmptyFullName() {
        final String currentDate = "2020-03-01";
        doReturn(LocalDate.parse(currentDate)).when(dateResolver).getCurrentDate();

        final String checkInDate = "2020-03-05";
        final String checkOutDate = "2020-03-08";
        final String email = "john.doe@email.com";
        final String fullName = " ";

        assertThatThrownBy(() ->
                Reservation.of(LocalDate.parse(checkInDate), LocalDate.parse(checkOutDate),
                        email, fullName, dateResolver)
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot create reservation with missing required field(s)");
    }

    @Test
    public void testCannotCreateReservationWithCheckOutDateBeforeChechInDate() {
        final String currentDate = "2020-03-01";
        doReturn(LocalDate.parse(currentDate)).when(dateResolver).getCurrentDate();

        final String checkInDate = "2020-03-08";
        final String checkOutDate = "2020-03-05";
        final String email = "john.doe@email.com";
        final String fullName = "JohnDoe";

        assertThatThrownBy(() ->
                Reservation.of(LocalDate.parse(checkInDate), LocalDate.parse(checkOutDate),
                        email, fullName, dateResolver)
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Check-out date must be after check-in date");
    }

    @Test
    public void testCannotCreateReservationForMoreThanThreeDays() {
        final String currentDate = "2020-03-01";
        doReturn(LocalDate.parse(currentDate)).when(dateResolver).getCurrentDate();

        final String checkInDate = "2020-03-05";
        final String checkOutDate = "2020-03-09";
        final String email = "john.doe@email.com";
        final String fullName = "JohnDoe";

        assertThatThrownBy(() ->
                Reservation.of(LocalDate.parse(checkInDate), LocalDate.parse(checkOutDate),
                        email, fullName, dateResolver)
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot reserve the campsite for more than 3 days");
    }

    @Test
    public void testCannotCreateReservationInThePast() {
        final String currentDate = "2020-03-01";
        doReturn(LocalDate.parse(currentDate)).when(dateResolver).getCurrentDate();

        final String checkInDate = "2020-02-15";
        final String checkOutDate = "2020-02-17";
        final String email = "john.doe@email.com";
        final String fullName = "JohnDoe";

        assertThatThrownBy(() ->
                Reservation.of(LocalDate.parse(checkInDate), LocalDate.parse(checkOutDate),
                        email, fullName, dateResolver)
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot reserve the campsite in the past");
    }

    @Test
    public void testCannotCreateReservationForTheCurrentDay() {
        final String currentDate = "2020-03-01";
        doReturn(LocalDate.parse(currentDate)).when(dateResolver).getCurrentDate();

        final String checkInDate = "2020-03-01";
        final String checkOutDate = "2020-03-02";
        final String email = "john.doe@email.com";
        final String fullName = "JohnDoe";

        assertThatThrownBy(() ->
                Reservation.of(LocalDate.parse(checkInDate), LocalDate.parse(checkOutDate),
                        email, fullName, dateResolver)
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot reserve the campsite for the current day");
    }

    @Test
    public void testCannotCreateReservationForMoreThanMonthInAdvance() {
        final String currentDate = "2020-03-01";
        doReturn(LocalDate.parse(currentDate)).when(dateResolver).getCurrentDate();

        final String checkInDate = "2020-05-01";
        final String checkOutDate = "2020-05-02";
        final String email = "john.doe@email.com";
        final String fullName = "JohnDoe";

        assertThatThrownBy(() ->
                Reservation.of(LocalDate.parse(checkInDate), LocalDate.parse(checkOutDate),
                        email, fullName, dateResolver)
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot reserve the campsite more than a month in advance");
    }

}
