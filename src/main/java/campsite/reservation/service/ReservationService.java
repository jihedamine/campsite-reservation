package campsite.reservation.service;

import campsite.reservation.concurrent.StripedLocks;
import campsite.reservation.data.entity.Reservation;
import campsite.reservation.data.repository.ReservationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static campsite.reservation.data.entity.Reservation.MAX_NB_DAYS_IN_MONTH;
import static java.time.temporal.ChronoUnit.DAYS;

/**
 * Provides services to process reservations
 */
@Component
@Transactional
public class ReservationService {

    private final static Logger logger = LoggerFactory.getLogger(ReservationService.class.getName());

    @Value("${request.maxWaitSeconds}")
    private int maxWaitSeconds; // maximum number of seconds to wait to acquire a lock

    @Autowired
    ReservationRepository repository;

    @Autowired
    DateResolver dateResolver;

    // StripedLocks is used to acquire a subset of locks representing a subset of dates
    // It allows to handle concurrent reservation requests on the same days
    private StripedLocks stripedLocks;

    // Boolean array representing the next month days availability.
    // A true value for index i means the campsite is reserved for the <current day + i day(s)>
    // A false value for index i means the campsite is free for the <current day + i day(s)>
    //
    // The in-memory reservedDates array allows handling large volumes of requests
    // to get the campsite availability as it removes the need to query the database
    private volatile boolean[] reservedDates;

    @PostConstruct
    public void init() {
        stripedLocks = new StripedLocks(MAX_NB_DAYS_IN_MONTH, maxWaitSeconds);
        reservedDates = new boolean[MAX_NB_DAYS_IN_MONTH];
        restoreReservations();
    }

    /**
     * Restore the reserved dates in-memory array using the database
     */
    private void restoreReservations() {
        List<Reservation> reservations = repository.findReservationsFromDate(dateResolver.getCurrentDate());
        for (Reservation reservation : reservations) {
            int reservationIndex = getDateIndex(reservation.getCheckInDate());
            reservedDates[reservationIndex] = true;
        }
    }

    /**
     * Returns a list of days where the campsite is available for reservation
     * @param nbDays number of days ahead to check
     * @return a list of days where the campsite is available for reservation
     */
    public List<LocalDate> getAvailableDates(int nbDays) {
        logger.info("Getting available dates list");

        if (nbDays > MAX_NB_DAYS_IN_MONTH) {
            nbDays = MAX_NB_DAYS_IN_MONTH; // nbDays can't be more that MAX_NB_DAYS_IN_MONTH
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

    /**
     * Makes a reservation, stores it in the database and updates the reserved dates array
     * @param reservation Reservation to store
     * @return the reservation id, if reservation was successful
     * @throws IllegalArgumentException if any of the reservation days are already booked
     */
    public String createReservation(Reservation reservation) throws IllegalArgumentException {
        logger.info("Creating new reservation {}", reservation);

        int startIndex = getDateIndex(reservation.getCheckInDate());
        int endIndex = getDateIndex(reservation.getCheckOutDate());

        Runnable saveFunction = () -> {
            throwExceptionIfAnyReservationDayIsAlreadyBooked(startIndex, endIndex);
            repository.save(reservation);
            updateReservedDates(startIndex, endIndex, true);
        };

        // acquire lock on the days, then perform the save operations
        stripedLocks.runSync(startIndex, endIndex, saveFunction);

        return reservation.getId();
    }

    /**
     * Updates an existing reservation.
     * Only the non-null fields in the reservationUpdate parameter
     * are applied to the reservation.
     * @param reservationId id of the reservation to update
     * @param reservationUpdate reservation update information
     * @return the updated reservation
     * @throws IllegalArgumentException if any of the reservation days are already booked
     * @throws EntityNotFoundException if there is no existing reservation with the reservation id
     */
    public Reservation updateReservation(String reservationId, Reservation reservationUpdate)
            throws IllegalArgumentException, EntityNotFoundException {
        logger.info("Updating reservation having id {} with {}", reservationId, reservationUpdate);

        Reservation reservation = repository.getOne(reservationId);

        Reservation originalReservation = Reservation.of(reservation, dateResolver);
        reservation.update(reservationUpdate);

        if (originalReservation.equals(reservation)) {
            return reservation; // Nothing changed, no need to update
        }

        // indexes of the reserved days on the original reservation
        int originalStartIndex = getDateIndex(originalReservation.getCheckInDate());
        int originalEndIndex = getDateIndex(originalReservation.getCheckOutDate());

        // indexes of the reserved days on the updated reservation
        int newStartIndex = getDateIndex(reservation.getCheckInDate());
        int newEndIndex = getDateIndex(reservation.getCheckOutDate());

        int lowestIndex = Math.min(originalStartIndex, newStartIndex);
        int highestIndex = Math.max(originalEndIndex, newEndIndex);

        Runnable updateFunction = () -> {
            updateReservedDates(originalStartIndex, originalEndIndex, false);
            throwExceptionIfAnyReservationDayIsAlreadyBooked(newStartIndex, newEndIndex);
            repository.save(reservation);
            updateReservedDates(newStartIndex, newEndIndex, true);
        };

        stripedLocks.runSync(lowestIndex, highestIndex, updateFunction);

        return Reservation.of(reservation, dateResolver);
    }

    /**
     * Cancel a reservation by marking its cancelled field to true
     * @param reservationId id of the reservation to cancel
     * @return the cancelled reservation
     * @throws EntityNotFoundException if there is no existing reservation with the reservation id
     */
    public Reservation cancelReservation(String reservationId) throws EntityNotFoundException {
        logger.info("Cancelling reservation with id {}", reservationId);

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

    /**
     * This method should be called at the end of a day as it shifts the reserved days array
     * to start from the next element (representing the next day)
     */
    public void moveReservedDatesToNextDay() {
        logger.info("Moving reserved dates to next day");
        System.arraycopy(reservedDates, 1, reservedDates, 0, reservedDates.length - 1);
    }

    // Used for tests to get a copy of the reserved dates array
    boolean[] getReservedDates() {
        return Arrays.copyOf(reservedDates, MAX_NB_DAYS_IN_MONTH);
    }

    // Used for tests to reset the reserved dates array
    void resetReservedDates() {
        reservedDates = new boolean[MAX_NB_DAYS_IN_MONTH];
    }

    private void throwExceptionIfAnyReservationDayIsAlreadyBooked(int newStartIndex, int newEndIndex) throws IllegalArgumentException {
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

    /**
     * Returns the number of days between the current date and the parameter date
     * @param date parameter date
     * @return number of days between the current date and the parameter date
     */
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
