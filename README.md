# Campsite Reservation

This project implements a simple REST API service to manage reservations on a campsite

- A reservation can be for a maximum of 3 days
- A reservation can be done up to 1 month in advance and minimally 1 day(s) ahead of arrival.
- Reservations can be cancelled anytime
- For sake of simplicity assume the check-in & check-out time is 12:00 AM


- The system should expose an API to provide information of the
  availability of the campsite for a given date range with the default being 1 month.

`GET http://<host>:<port>/reservation/availableDates?range=<number-of-days>`

- Make a reservation, providing
  - email
  - full name
  - arrival date
  - departure date

If a reservation was done, a unique reservation identifier is returned to the API client

`POST http://<host>:<port>/reservation?email=<email>&fullName=<fullname>&arrivalDate=<ddmmYYYY>&departureDate=<ddmmYYYY>`

- Modify a reservation

`PUT http://<host>:<port>/reservation/<reservation-id>[?email=<new-email>][&fullName=<new-fullname>&arrivalDate=<ddmmYYYY>&departureDate=<ddmmYYYY>`

- Cancel a reservation

`DELETE http://<host>:<port>/reservation/<reservation-id>`

The system:
- Gracefully handles concurrent requests to reserve the campsite.
- Provides appropriate error messages to the caller to indicate the error cases.
- Is able to handle large volume of requests for getting the campsite availability.
