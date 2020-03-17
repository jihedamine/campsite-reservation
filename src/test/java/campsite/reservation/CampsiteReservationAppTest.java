package campsite.reservation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CampsiteReservationAppTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void restAPIIntegrationTest() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        // Create a reservation

        String checkInDate = LocalDate.now().plusDays(3).toString();
        String checkOutDate = LocalDate.now().plusDays(5).toString();
        String email = "john.doe@email.com";
        String fullName = "JohnDoe";

        ObjectNode reservation = mapper.createObjectNode();
        reservation.put("checkInDate", checkInDate);
        reservation.put("checkOutDate", checkOutDate);
        reservation.put("email", email);
        reservation.put("fullName", fullName);

        MvcResult createReservationResult = mockMvc.perform(post("/reservations")
                .content(reservation.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isCreated()).andReturn();

        JsonNode reservationIdJson = mapper.readTree(createReservationResult.getResponse().getContentAsString());
        String reservationId = reservationIdJson.get("reservationId").asText();

        // Update the reservation

        String newCheckInDate = LocalDate.now().plusDays(4).toString();
        String newCheckOutDate = LocalDate.now().plusDays(6).toString();
        String newEmail = "newEmail";
        String newFullName = "newFullName";
        reservation.put("checkInDate", newCheckInDate);
        reservation.put("checkOutDate", newCheckOutDate);
        reservation.put("email", newEmail);
        reservation.put("fullName", newFullName);

        MvcResult updateReservationResult = mockMvc.perform(put("/reservations/{id}", reservationId)
                .content(reservation.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().is(200)).andReturn();

        JsonNode updatedReservationJson = mapper.readTree(updateReservationResult.getResponse().getContentAsString());
        assertThat(updatedReservationJson.get("id").asText()).isEqualTo(reservationId);
        assertThat(updatedReservationJson.get("checkInDate").asText()).isEqualTo(newCheckInDate);
        assertThat(updatedReservationJson.get("checkOutDate").asText()).isEqualTo(newCheckOutDate);
        assertThat(updatedReservationJson.get("email").asText()).isEqualTo(newEmail);
        assertThat(updatedReservationJson.get("fullName").asText()).isEqualTo(newFullName);

        // Get available dates

        MvcResult availableDatesResult = mockMvc.perform(get("/reservations/availableDates")
                .param("nbDays", "7")
                .content(reservation.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().is(200)).andReturn();
        JsonNode availableDatesJson = mapper.readTree(availableDatesResult.getResponse().getContentAsString());

        ArrayNode availableDates = (ArrayNode) availableDatesJson.get("dates");
        assertThat(availableDates).hasSize(4);
        assertThat(availableDates.get(0).asText()).isEqualTo(LocalDate.now().plusDays(1).toString());
        assertThat(availableDates.get(1).asText()).isEqualTo(LocalDate.now().plusDays(2).toString());
        assertThat(availableDates.get(2).asText()).isEqualTo(LocalDate.now().plusDays(3).toString());
        assertThat(availableDates.get(3).asText()).isEqualTo(LocalDate.now().plusDays(7).toString());

        // Cancel the reservation

        MvcResult cancelReservationResult = mockMvc.perform(delete("/reservations/{id}", reservationId)
                .content(reservation.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().is(200)).andReturn();

        JsonNode cancelledReservationJson = mapper.readTree(cancelReservationResult.getResponse().getContentAsString());
        assertThat(cancelledReservationJson.get("id").asText()).isEqualTo(reservationId);
        assertThat(cancelledReservationJson.get("cancelled").asBoolean()).isTrue();
    }

}