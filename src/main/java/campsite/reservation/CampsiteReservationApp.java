package campsite.reservation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"campsite.data.repository", "campsite"})
public class CampsiteReservationApp {

    public static void main(String[] args) {
        SpringApplication.run(CampsiteReservationApp.class, args);
    }
}
