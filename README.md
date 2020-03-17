# Campsite Reservation

This project implements a simple REST API service to manage reservations on a campsite

- A reservation can be for a maximum of 3 days
- A reservation can be done up to 1 month in advance and minimally 1 day(s) ahead of arrival.
- Reservations can be cancelled anytime
- For the sake of simplicity, the check-in & check-out time is 12:00 AM

## Rest endpoints

The system exposes a REST API to:

- Provide a list of available dates for a given range of days (with the default being 30 days) to make a reservation.

```
curl -X "GET" http://<host>:<port>/reservations/availableDates?nbDays=<number-of-days>`
```

- Make a reservation, the check-in date, check-out date, email and full name of the reserving person. If the reservation request succeeded, a unique reservation identifier is returned to the API caller.

```
curl -X "POST" "http://<host>:<port>/reservations"
   -i
   -H 'Content-Type: application/json'
   -d $'{
    "checkInDate": "2020-03-01",
    "checkOutDate": "2020-03-08",
    "fullName": "John Doe",
    "email": "john.doe@email.com"
   }'
```

- Modify a reservation using its id

```
curl -X "PUT" "http://<host>:<port>/reservations/<reservation-id>"
   -i
   -H 'Content-Type: application/json'
   -d $'{
    "checkInDate": "2020-03-01",
    "checkOutDate": "2020-03-08",
    "fullName": "John Doe",
    "email": "john.doe@email.com"
   }'
```

- Cancel a reservation using its id

```
curl -X "DELETE" http://<host>:<port>/reservations/<reservation-id>`
```

The system:
- Gracefully handles concurrent requests to reserve the campsite.
- Is able to handle a large volume of requests for getting the campsite availability.
- Provides appropriate error messages to the caller to indicate the error cases.

## Reservation dates

- The dates represent local dates in the campsite timezone.
- As the check-in and check-out times are 12:00 AM, we omit the time portion when modeling dates.

## Application parameters

The property ```request.maxWaitSeconds``` in the properties file ```application.properties``` allows setting the maximum number of seconds to wait to acquire a lock in order to perform an operation on a reservation that requires synchronization.

## Running the application

The source code language level is Java 11, so you need a JDK 11 or a more recent version to compile the code.

To run the application, clone this repository and open a terminal at the root folder of the repository.

Either build a jar and run it:

```
$> mvn package
$> java -jar target/wikipedia_search-1.0-SNAPSHOT.jar
```

Or directly run the spring boot maven goal

```
$> mvn spring-boot:run
```