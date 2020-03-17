package campsite.reservation.service;

import campsite.reservation.data.entity.Reservation;
import campsite.reservation.data.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@SpringBootTest
class ReservationServiceTest {

    @Autowired
    ReservationService reservationService;

    @MockBean
    DateResolver dateResolver;

    @Autowired
    ReservationRepository repository;

    @BeforeEach
    void cleanUp() {
        repository.deleteAll();
        reservationService.resetReservedDates();
    }

    @Test
    void getAvailableDates() {
        final String currentDate = "2020-03-01";
        final String checkInDate = "2020-03-03";
        final String checkOutDate = "2020-03-05";
        final String email = "john.doe@email.com";
        final String fullName = "JohnDoe";

        given(dateResolver.getCurrentDate()).willReturn(LocalDate.parse(currentDate));

        Reservation reservation = Reservation.of(LocalDate.parse(checkInDate),
                LocalDate.parse(checkOutDate),
                email, fullName, dateResolver);

        reservationService.createReservation(reservation);
        List<LocalDate> availableDates = reservationService.getAvailableDates(6);
        assertThat(availableDates).hasSize(3);
        assertThat(availableDates.get(0)).isEqualTo("2020-03-02");
        assertThat(availableDates.get(1)).isEqualTo("2020-03-06");
        assertThat(availableDates.get(2)).isEqualTo("2020-03-07");
    }

    @Test
    void createReservationMoreThanOneThread() throws InterruptedException {
        final String currentDate = "2020-03-01";
        final String checkInDate = "2020-03-05";
        final String checkOutDate = "2020-03-08";
        final String email1 = "john.doe@email.com";
        final String fullName1 = "JohnDoe";
        final String email2 = "john.doe2@email.com";
        final String fullName2 = "JohnDoe2";

        given(dateResolver.getCurrentDate()).willReturn(LocalDate.parse(currentDate));

        Reservation reservation1 = Reservation.of(LocalDate.parse(checkInDate),
                LocalDate.parse(checkOutDate),
                email1, fullName1, dateResolver);

        Reservation reservation2 = Reservation.of(LocalDate.parse(checkInDate),
                LocalDate.parse(checkOutDate),
                email2, fullName2, dateResolver);

        ExecutorService executorService = Executors.newFixedThreadPool(2);
        Callable<String> reservationRequest1 = () -> reservationService.createReservation(reservation1);
        Callable<String> reservationRequest2 = () -> reservationService.createReservation(reservation2);
        List<Future<String>> futures = executorService.invokeAll(Arrays.asList(reservationRequest1, reservationRequest2));

        assertThatThrownBy(() -> {
            for (Future<String> future : futures) {
                future.get();
            }
        }).hasCauseInstanceOf(IllegalArgumentException.class)
                .hasMessageEndingWith("Day already reserved 2020-03-05");
    }

    @Test
    void createConcurrentReservationsNotOverlappingDatesSucceeds() throws InterruptedException, ExecutionException {
        final String currentDate = "2020-03-01";
        given(dateResolver.getCurrentDate()).willReturn(LocalDate.parse(currentDate));

        final String checkInDate1 = "2020-03-05";
        final String checkOutDate1 = "2020-03-08";
        final String email1 = "john.doe@email.com";
        final String fullName1 = "JohnDoe";

        final String checkInDate2 = "2020-03-10";
        final String checkOutDate2 = "2020-03-11";
        final String email2 = "john.doe2@email.com";
        final String fullName2 = "JohnDoe2";

        Reservation reservation1 = Reservation.of(LocalDate.parse(checkInDate1),
                LocalDate.parse(checkOutDate1),
                email1, fullName1, dateResolver);

        Reservation reservation2 = Reservation.of(LocalDate.parse(checkInDate2),
                LocalDate.parse(checkOutDate2),
                email2, fullName2, dateResolver);

        ExecutorService executorService = Executors.newFixedThreadPool(2);
        Callable<String> reservationRequest1 = () -> reservationService.createReservation(reservation1);
        Callable<String> reservationRequest2 = () -> reservationService.createReservation(reservation2);
        List<Future<String>> futures = executorService.invokeAll(Arrays.asList(reservationRequest1, reservationRequest2));

        List<String> reservationIds = new ArrayList<>();
        for (Future<String> future : futures) {
            reservationIds.add(future.get());
        }

        assertThat(reservationIds).hasSize(2);
    }

    @Test
    void createConcurrentReservationsOverlappingDatesThrowsException() throws InterruptedException {
        final String currentDate = "2020-03-01";
        given(dateResolver.getCurrentDate()).willReturn(LocalDate.parse(currentDate));

        final String checkInDate1 = "2020-03-05";
        final String checkOutDate1 = "2020-03-08";
        final String email1 = "john.doe@email.com";
        final String fullName1 = "JohnDoe";

        final String checkInDate2 = "2020-03-07";
        final String checkOutDate2 = "2020-03-09";
        final String email2 = "john.doe2@email.com";
        final String fullName2 = "JohnDoe2";

        Reservation reservation1 = Reservation.of(LocalDate.parse(checkInDate1),
                LocalDate.parse(checkOutDate1),
                email1, fullName1, dateResolver);

        Reservation reservation2 = Reservation.of(LocalDate.parse(checkInDate2),
                LocalDate.parse(checkOutDate2),
                email2, fullName2, dateResolver);

        ExecutorService executorService = Executors.newFixedThreadPool(2);
        Callable<String> reservationRequest1 = () -> reservationService.createReservation(reservation1);
        Callable<String> reservationRequest2 = () -> reservationService.createReservation(reservation2);
        List<Future<String>> futures = executorService.invokeAll(Arrays.asList(reservationRequest1, reservationRequest2));

        assertThatThrownBy(() -> {
            for (Future<String> future : futures) {
                future.get();
            }
        }).hasCauseInstanceOf(IllegalArgumentException.class)
                .hasMessageEndingWith("Day already reserved 2020-03-07");
    }

    @Test
    void cancelReservationFreesDate() {
        final String currentDate = "2020-03-01";
        given(dateResolver.getCurrentDate()).willReturn(LocalDate.parse(currentDate));

        final String checkInDate = "2020-03-03";
        final String checkOutDate = "2020-03-04";
        final String email1 = "john.doe@email.com";
        final String fullName1 = "JohnDoe";

        Reservation reservation = Reservation.of(LocalDate.parse(checkInDate),
                LocalDate.parse(checkOutDate),
                email1, fullName1, dateResolver);

        String reservationId = reservationService.createReservation(reservation);

        List<LocalDate> availableDates = reservationService.getAvailableDates(5);
        assertThat(availableDates).hasSize(3);
        assertThat(availableDates.get(0)).isEqualTo("2020-03-02");
        assertThat(availableDates.get(1)).isEqualTo("2020-03-05");
        assertThat(availableDates.get(2)).isEqualTo("2020-03-06");

        Reservation updatedReservation = reservationService.cancelReservation(reservationId);

        assertThat(updatedReservation.isCancelled()).isTrue();

        availableDates = reservationService.getAvailableDates(5);
        assertThat(availableDates).hasSize(5);
        assertThat(availableDates.get(0)).isEqualTo("2020-03-02");
        assertThat(availableDates.get(1)).isEqualTo("2020-03-03");
        assertThat(availableDates.get(2)).isEqualTo("2020-03-04");
        assertThat(availableDates.get(3)).isEqualTo("2020-03-05");
        assertThat(availableDates.get(4)).isEqualTo("2020-03-06");
    }

    @Test
    public void testMovingSlice() {
        final String currentDate = "2020-03-01";
        given(dateResolver.getCurrentDate()).willReturn(LocalDate.parse(currentDate));

        String checkInDate = "2020-03-03";
        String checkOutDate = "2020-03-05";
        String email = "john.doe@email.com";
        String fullName = "JohnDoe";

        Reservation reservation = Reservation.of(LocalDate.parse(checkInDate),
                LocalDate.parse(checkOutDate),
                email, fullName, dateResolver);

        reservationService.createReservation(reservation);

        boolean[] reservedDates = reservationService.getReservedDates();
        assertThat(reservedDates[0]).isFalse();
        assertThat(reservedDates[1]).isTrue();
        assertThat(reservedDates[2]).isTrue();
        assertThat(reservedDates[3]).isTrue();
        assertThat(reservedDates[4]).isFalse();
        assertThat(reservedDates[30]).isFalse();

        given(dateResolver.getCurrentDate()).willReturn(LocalDate.parse(currentDate).plusDays(1));

        // Move to next day
        reservationService.moveReservedDatesToNextDay();

        // Assert that reserved dates got shifted by one day

        reservedDates = reservationService.getReservedDates();
        assertThat(reservedDates[0]).isTrue();
        assertThat(reservedDates[1]).isTrue();
        assertThat(reservedDates[2]).isTrue();
        assertThat(reservedDates[3]).isFalse();
        assertThat(reservedDates[4]).isFalse();
        assertThat(reservedDates[30]).isFalse();
    }
}