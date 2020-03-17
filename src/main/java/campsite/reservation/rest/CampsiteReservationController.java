package campsite.reservation.rest;

import campsite.reservation.data.entity.Reservation;
import campsite.reservation.serialization.types.DatesList;
import campsite.reservation.serialization.types.ReservationId;
import campsite.reservation.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/reservations")
public class CampsiteReservationController {

    @Autowired
    ReservationService reservationService;

    @GetMapping(value = "/availableDates")
    DatesList getAvailableDates(@RequestParam int nbDays) {
        return new DatesList(reservationService.getAvailableDates(nbDays));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    ReservationId createReservation(@RequestBody Reservation reservation) {
        return new ReservationId(reservationService.createReservation(reservation));
    }

    @PutMapping(value = "/{reservationId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    Reservation updateReservation(@PathVariable("reservationId") String reservationId,
                                  @RequestBody Reservation reservationUpdate) {
        return reservationService.updateReservation(reservationId, reservationUpdate);
    }

    @DeleteMapping(value = "/{reservationId}")
    Reservation cancelReservation(@PathVariable("reservationId") String reservationId) {
        return reservationService.cancelReservation(reservationId);
    }

    @Scheduled(cron = "0 0 * * *")
    public void scheduleTaskUsingCronExpression() {
        reservationService.moveReservedDatesToNextDay();
    }

}
