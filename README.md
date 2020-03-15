# Campsite Reservation

This project implements a simple REST API service to manage reservations on a campsite

- A reservation can be for a maximum of 3 days
- A reservation can be done up to 1 month in advance and minimally 1 day(s) ahead of arrival.
- Reservations can be cancelled anytime
- For the sake of simplicity, the check-in & check-out time is 12:00 AM

## Rest endpoints

The system exposes a REST API to:

- Provide a list of available dates for a given range of days (with the default being 30 days) to make a reservation.

`GET http://<host>:<port>/reservation/availableDates?nbDays=<number-of-days>`

- Make a reservation, providing
  - email
  - full name
  - arrival date
  - departure date

If the reservation request succeeded, a unique reservation identifier is returned to the API caller.

`POST http://<host>:<port>/reservation?email=<email>&fullName=<fullname>&arrivalDate=<ddmmYYYY>&departureDate=<ddmmYYYY>`

- Modify a reservation

`PUT http://<host>:<port>/reservation/<reservation-id>[?email=<new-email>][&fullName=<new-fullname>&arrivalDate=<ddmmYYYY>&departureDate=<ddmmYYYY>`

- Cancel a reservation

`DELETE http://<host>:<port>/reservation/<reservation-id>`

The system:
- Gracefully handles concurrent requests to reserve the campsite.
- Is able to handle a large volume of requests for getting the campsite availability.
- Provides appropriate error messages to the caller to indicate the error cases.
