package campsite.reservation.data.entity;

import campsite.reservation.service.DateResolver;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.logging.log4j.util.Strings;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

import static java.time.temporal.ChronoUnit.DAYS;

/**
 * Represents a reservation.
 * Contains validation checks that have to succeed in order to create a reservation instance
 * The check-in and check-out dates represent local dates in the campsite timezone.
 * As the check-in and check-out times are always 12:00 AM, we omit the time portion when modeling dates.
 */
@Entity
public class Reservation {

    public static final int MAX_NB_DAYS_IN_MONTH = 31;

    @Id
    private String id;

    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private String email;
    private String fullName;
    private boolean isCancelled;

    // Needed by hibernate
    public Reservation() {
    }

    private Reservation(LocalDate checkInDate, LocalDate checkOutDate, String email, String fullName) {
        this(UUID.randomUUID().toString(), checkInDate, checkOutDate, email, fullName, false);
    }

    private Reservation(String id, LocalDate checkInDate, LocalDate checkOutDate,
                        String email, String fullName, boolean isCancelled) {
        this.id = id;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.email = email;
        this.fullName = fullName;
        this.isCancelled = isCancelled;
    }

    /**
     * Obtains an instance of {@code Reservation}
     *
     * @param checkInDate Reservation check-in date
     * @param checkOutDate Reservation check-out date
     * @param email Email of the person making the reservation
     * @param fullName Full name of the person making the reservation
     * @return the reservation
     */
    @JsonCreator
    public static Reservation of(@JsonProperty("checkInDate") LocalDate checkInDate,
                                 @JsonProperty("checkOutDate") LocalDate checkOutDate,
                                 @JsonProperty("email") String email,
                                 @JsonProperty("fullName") String fullName) {
        return of(checkInDate, checkOutDate, email, fullName, null);
    }

    /**
     * Obtains an instance of a {@code Reservation} by copying an existing reservation
     *
     * @param reservation Reservation to copy
     * @param dateResolver Date resolver to get the current day
     * @return the reservation
     */
    public static Reservation of(Reservation reservation,
                                 DateResolver dateResolver) {
        return of(reservation.getId(), reservation.getCheckInDate(), reservation.getCheckOutDate(),
                reservation.getEmail(), reservation.getFullName(), reservation.isCancelled(), dateResolver);
    }

    /**
     * Obtains an instance of a {@code Reservation}
     *
     * @param id Reservation's unique identifier
     * @param checkInDate Reservation check-in date
     * @param checkOutDate Reservation check-out date
     * @param email Email of the person making the reservation
     * @param fullName Full name of the person making the reservation
     * @param isCancelled true if the reservation was cancelled, false otherwise
     * @param dateResolver Date resolver to get the current day
     * @return the reservation
     */
    public static Reservation of(String id,
                                 LocalDate checkInDate,
                                 LocalDate checkOutDate,
                                 String email,
                                 String fullName,
                                 boolean isCancelled,
                                 DateResolver dateResolver) {
        if (id == null) {
            throw new IllegalArgumentException("Cannot create reservation with missing required field(s)");
        }

        validateParameters(checkInDate, checkOutDate, email, fullName, dateResolver);

        return new Reservation(id, checkInDate, checkOutDate, email, fullName, isCancelled);
    }

    /**
     * Obtains an instance of a {@code Reservation}
     *
     * @param checkInDate Reservation check-in date
     * @param checkOutDate Reservation check-out date
     * @param email Email of the person making the reservation
     * @param fullName Full name of the person making the reservation
     * @param dateResolver Date resolver to get the current day
     * @return the reservation
     */
    public static Reservation of(LocalDate checkInDate,
                                 LocalDate checkOutDate,
                                 String email,
                                 String fullName,
                                 DateResolver dateResolver) {
        validateParameters(checkInDate, checkOutDate, email, fullName, dateResolver);

        return new Reservation(checkInDate, checkOutDate, email, fullName);
    }

    /**
     * Validates that the parameters used to create a reservation have valid values.
     * Validations are:
     * - Cannot create a reservation with missing required field(s)
     * - Check-out date must be after check-in date
     * - Cannot reserve the campsite for more than 3 days
     * - Cannot reserve the campsite in the past
     * - Cannot reserve the campsite for the current day
     * - Cannot reserve the campsite more than a month in advance
     * @throws IllegalArgumentException if any validation of the parameters fails.
     */
    private static void validateParameters(LocalDate checkInDate, LocalDate checkOutDate, String email, String fullName, DateResolver dateResolver) {
        if ((checkInDate == null) || (checkOutDate == null) || (Strings.isBlank(email)) || (Strings.isBlank(fullName))) {
            throw new IllegalArgumentException("Cannot create reservation with missing required field(s)");
        }

        long checkOutMinusCheckInDays = DAYS.between(checkInDate, checkOutDate);

        if (checkOutMinusCheckInDays < 0) {
            throw new IllegalArgumentException("Check-out date must be after check-in date");
        }

        if (checkOutMinusCheckInDays > 3) {
            throw new IllegalArgumentException("Cannot reserve the campsite for more than 3 days");
        }

        LocalDate currentDate = getCurrentDate(dateResolver);

        long todayMinusCheckInDate = DAYS.between(currentDate, checkInDate);

        if (todayMinusCheckInDate < 0) {
            throw new IllegalArgumentException("Cannot reserve the campsite in the past");
        }

        if (todayMinusCheckInDate == 0) {
            throw new IllegalArgumentException("Cannot reserve the campsite for the current day");
        }

        if (todayMinusCheckInDate > MAX_NB_DAYS_IN_MONTH) {
            throw new IllegalArgumentException("Cannot reserve the campsite more than a month in advance");
        }
    }

    private static LocalDate getCurrentDate(DateResolver dateResolver) {
        LocalDate currentDate;
        if (dateResolver == null) {
            currentDate = LocalDate.now();
        } else {
            currentDate = dateResolver.getCurrentDate();
        }
        return currentDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reservation that = (Reservation) o;
        return isCancelled == that.isCancelled &&
                Objects.equals(checkInDate, that.checkInDate) &&
                Objects.equals(checkOutDate, that.checkOutDate) &&
                Objects.equals(email, that.email) &&
                Objects.equals(fullName, that.fullName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(checkInDate, checkOutDate, email, fullName, isCancelled);
    }

    @Override
    public String toString() {
        return "Reservation{" +
                "id='" + id + '\'' +
                ", checkInDate=" + checkInDate +
                ", checkOutDate=" + checkOutDate +
                ", email='" + email + '\'' +
                ", fullName='" + fullName + '\'' +
                ", isCancelled=" + isCancelled +
                '}';
    }

    public void update(Reservation reservationUpdate) {
        setCheckInDate(reservationUpdate.getCheckInDate());
        setCheckOutDate(reservationUpdate.getCheckOutDate());
        setEmail(reservationUpdate.getEmail());
        setFullName(reservationUpdate.getFullName());
    }

    public String getId() {
        return id;
    }

    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(LocalDate checkInDate) {
        if (checkInDate == null) {
            return;
        }
        this.checkInDate = checkInDate;
    }

    public LocalDate getCheckOutDate() {
        return checkOutDate;
    }

    public void setCheckOutDate(LocalDate checkOutDate) {
        if (checkOutDate == null) {
            return;
        }
        this.checkOutDate = checkOutDate;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        if (email == null) {
            return;
        }
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        if (fullName == null) {
            return;
        }
        this.fullName = fullName;
    }

    public boolean isCancelled() {
        return isCancelled;
    }

    public void setCancelled(boolean cancelled) {
        isCancelled = cancelled;
    }

}
