package cloud.sonam.reactiverestservice;

import cloud.sonam.db.entity.Employee;
import cloud.sonam.rest.EmployeeRestService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = EmployeeRestService.class)
public class EmployeeWebClientTest {

    @Autowired
    private WebTestClient client;

    private static final Logger LOG = LoggerFactory.getLogger(EmployeeWebClientTest.class);

    @Test
    public void getEmployeeById() {
        client.get().uri("/employees/id/{id}", 1L).
                accept(MediaType.APPLICATION_JSON)
                .exchange().expectStatus().isOk()
                .expectBody(Employee.class).consumeWith(
                        result -> {
                            LOG.info("got employee: {}", result.getResponseBody());
                        }
        );
    }

    @Test
    public void getEmployeeByFirstName() {
        client.get().uri("/employees/name/{firstName}", "sonam")
                //.accept(MediaType.APPLICATION_JSON)
                .exchange().expectStatus().isOk()
                .expectBodyList(Employee.class)
                .consumeWith(
                result -> {
                    LOG.info("got employee by first name: {}", result.getResponseBody());
                }
        );
    }



}
