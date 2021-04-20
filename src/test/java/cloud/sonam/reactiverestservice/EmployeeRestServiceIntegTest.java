package cloud.sonam.reactiverestservice;

import cloud.sonam.db.entity.Employee;
import cloud.sonam.db.service.impl.EmployeeService;
import lombok.extern.java.Log;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

@AutoConfigureWebTestClient
@Log
@RunWith(SpringRunner.class)
@SpringBootTest
public class EmployeeRestServiceIntegTest {

    @Autowired
    private WebTestClient client;

    private static final Logger LOG = LoggerFactory.getLogger(EmployeeRestServiceIntegTest.class);

    @Test
    public void hello() {

        client.get().uri("/employees")
                .exchange().expectStatus().isOk()
                .expectBody(Employee.class)
                .consumeWith(result -> {
                    LOG.info("employee saved {}", result.getResponseBody());
                });
    }

    @Before
    public void save() {
        client.post().uri("/employees")
                .body(Mono.just(new Employee(null, "sonam")), Employee.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange().expectStatus().isOk()
                .expectBody(Employee.class)
                .consumeWith(result -> {
                    LOG.info("employee saved {}", result.getResponseBody());
                });

    }

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
        client.get().uri("/employees/name/{name}", "sonam")
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
