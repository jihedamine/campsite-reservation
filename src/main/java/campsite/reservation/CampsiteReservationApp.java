package campsite.reservation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import java.net.InetAddress;
import java.net.UnknownHostException;

@SpringBootApplication
public class CampsiteReservationApp {
    private final static Logger logger = LoggerFactory.getLogger(CampsiteReservationApp.class.getName());

    public static void main(String[] args) {
        ApplicationContext applicationContext = SpringApplication.run(CampsiteReservationApp.class, args);

        StringBuilder endpointPrefix = new StringBuilder("http://");
        endpointPrefix.append(getHostAddress());
        endpointPrefix.append(":");
        endpointPrefix.append(getHostPort(applicationContext));

        System.out.println(getEndpointsMessage(endpointPrefix.toString()));
    }

    private static String getEndpointsMessage(String endpointPrefix) {
        return "The application exposes a REST API that provides the following capabilities:\n" +
                "- Provide a list of available dates for a given range of days (with the default being 30 days) to make a reservation\n" +
                "GET " + endpointPrefix + "/reservations/availableDates?nbDays=<number-of-days>\n" +
                "- Make a reservation, providing the check-in date, check-out date, email and full name of the reserving person:\n" +
                "curl -X \"POST\" \"" + endpointPrefix + "/reservations\"\n" +
                "   -i\n" +
                "   -H 'Content-Type: application/json'\n" +
                "   -d $'{\n" +
                "    \"checkInDate\": \"2020-03-01\",\n" +
                "    \"checkOutDate\": \"2020-03-08\",\n" +
                "    \"fullName\": \"John Doe\",\n" +
                "    \"email\": \"john.doe@email.com\"\n" +
                "   }'\n" +
                "- Modify a reservation using the reservation id:\n" +
                "curl -X \"PUT\" \"" + endpointPrefix + "/reservations/<reservation-id>\"\n" +
                "   -i\n" +
                "   -H 'Content-Type: application/json'\n" +
                "   -d $'{\n" +
                "    \"checkInDate\": \"2020-03-01\",\n" +
                "    \"checkOutDate\": \"2020-03-08\",\n" +
                "    \"fullName\": \"John Doe\",\n" +
                "    \"email\": \"john.doe@email.com\"\n" +
                "   }'\n" +
                "- Cancel a reservation:\n" +
                "curl -X \"DELETE\" " + endpointPrefix + "/reservations/<reservation-id>\n" +
                "All responses from the API are in JSON format.";
    }

    private static Integer getHostPort(ApplicationContext applicationContext) {
        return applicationContext.getBean(Environment.class).getProperty("server.port", Integer.class, 8080);
    }

    private static String getHostAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            logger.warn("Cannot determine host address", e);
            return "host";
        }
    }
}
