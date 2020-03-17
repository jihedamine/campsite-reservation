package campsite.reservation.service;

import campsite.reservation.concurrent.StripedLocks;
import campsite.reservation.data.entity.Reservation;
import campsite.reservation.data.repository.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static campsite.reservation.data.entity.Reservation.MAX_NB_DAYS_IN_MONTH;
import static java.time.temporal.ChronoUnit.DAYS;

@Component
@Transactional
public class ReservationService {

    @Value("${request.maxWaitSeconds}")
    private int maxWaitSeconds;

    @Autowired
    ReservationRepository repository;

    @Autowired
    DateResolver dateResolver;

    private StripedLocks stripedLocks;

    private volatile boolean[] reservedDates;

    @PostConstruct
    public void init() {
        stripedLocks = new StripedLocks(MAX_NB_DAYS_IN_MONTH, maxWaitSeconds);
        reservedDates = new boolean[MAX_NB_DAYS_IN_MONTH];
        restoreReservations();
    }

    private void restoreReservations() {
        List<Reservation> reservations = repository.findReservationsFromDate(dateResolver.getCurrentDate());
        for (Reservation reservation : reservations) {
            int reservationIndex = getDateIndex(reservation.getCheckInDate());
            reservedDates[reservationIndex] = true;
        }
    }

    public List<LocalDate> getAvailableDates(int nbDays) {
        if (nbDays > MAX_NB_DAYS_IN_MONTH) {
            nbDays = MAX_NB_DAYS_IN_MONTH;
        }

        List<LocalDate> localDates = new ArrayList<>();
        LocalDate localDate = dateResolver.getCurrentDate();
        for (int i = 0; i < nbDays; i++) {
            localDate = localDate.plusDays(1);
            if (!reservedDates[i]) {
                localDates.add(localDate);
            }
        }
        return localDates;
    }

    public String createReservation(Reservation reservation) {
        int startIndex = getDateIndex(reservation.getCheckInDate());
        int endIndex = getDateIndex(reservation.getCheckOutDate());

        Runnable saveFunction = () -> {
            throwExceptionIfReservationDatesAreNotFree(startIndex, endIndex);
            repository.save(reservation);
            updateReservedDates(startIndex, endIndex, true);
        };

        stripedLocks.runSync(startIndex, endIndex, saveFunction);

        return reservation.getId();
    }

    public Reservation updateReservation(String reservationId, Reservation reservationUpdate) {
        Reservation reservation = repository.getOne(reservationId);

        Reservation originalReservation = Reservation.of(reservation, dateResolver);
        reservation.update(reservationUpdate);

        if (originalReservation.equals(reservation)) {
            return reservation; // Nothing changed, no need to update
        }

        int originalStartIndex = getDateIndex(originalReservation.getCheckInDate());
        int originalEndIndex = getDateIndex(originalReservation.getCheckOutDate());

        int newStartIndex = getDateIndex(reservation.getCheckInDate());
        int newEndIndex = getDateIndex(reservation.getCheckOutDate());

        int lowestIndex = Math.min(originalStartIndex, newStartIndex);
        int highestIndex = Math.max(originalEndIndex, newEndIndex);

        Runnable updateFunction = () -> {
            updateReservedDates(originalStartIndex, originalEndIndex, false);

            throwExceptionIfReservationDatesAreNotFree(newStartIndex, newEndIndex);

            repository.save(reservation);

            updateReservedDates(newStartIndex, newEndIndex, true);
        };

        stripedLocks.runSync(lowestIndex, highestIndex, updateFunction);

        return Reservation.of(reservation, dateResolver);
    }

    public Reservation cancelReservation(String reservationId) {
        Reservation reservation = repository.getOne(reservationId);
        reservation.setCancelled(true);
        int startIndex = getDateIndex(reservation.getCheckInDate());
        int endIndex = getDateIndex(reservation.getCheckOutDate());

        Runnable cancelFunction = () -> {
            repository.save(reservation);
            updateReservedDates(startIndex, endIndex, false);
        };

        stripedLocks.runSync(startIndex, endIndex, cancelFunction);

        return Reservation.of(reservation, dateResolver);
    }

    public void moveReservedDatesToNextDay() {
        System.arraycopy(reservedDates, 1, reservedDates, 0, reservedDates.length - 1);
    }

    boolean[] getReservedDates() {
        return Arrays.copyOf(reservedDates, MAX_NB_DAYS_IN_MONTH);
    }

    void resetReservedDates() {
        reservedDates = new boolean[MAX_NB_DAYS_IN_MONTH];
    }

    private void throwExceptionIfReservationDatesAreNotFree(int newStartIndex, int newEndIndex) {
        for (int i = newStartIndex; i <= newEndIndex; i++) {
            if (reservedDates[i]) {
                throw new IllegalArgumentException("Day already reserved " + dateResolver.getCurrentDate().plusDays(i + 1));
            }
        }
    }

    private void updateReservedDates(int startIndex, int endIndex, boolean isReserved) {
        for (int i = startIndex; i <= endIndex; i++) {
            reservedDates[i] = isReserved;
        }
    }

    private int getDateIndex(LocalDate date) {
        try {
            return Math.toIntExact(DAYS.between(dateResolver.getCurrentDate(), date)) - 1;
        } catch (ArithmeticException e) {
            throw new RuntimeException("Difference between current date "
                    + dateResolver.getCurrentDate()
                    + " and date "
                    + date
                    + " is too big");
        }
    }
}
