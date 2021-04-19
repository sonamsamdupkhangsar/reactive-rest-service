package cloud.sonam.reactiverestservice;

import cloud.sonam.db.entity.Employee;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class EmployeeWebClient {
    WebClient client = WebClient.create("http://localhost:8080");

    private static final Logger LOG = LoggerFactory.getLogger(EmployeeWebClient.class);


    public void getEmployee() {
        LOG.info("get mono of employee");
        Mono<Employee> employeeMono = client.get().uri("/employees/id/{id}", 1L)
                .retrieve().bodyToMono(Employee.class);
        LOG.info("printing mono employee: {}", employeeMono);

        employeeMono.subscribe(employee -> LOG.info("mono employee: {}", employee));
    }

    public void getEmployees() {
        LOG.info("get flux of employees");
        Flux<Employee> flux = client.get().uri("/employees/name/{fName}", "dum")
                .retrieve().bodyToFlux(Employee.class);
        LOG.info("printing flux employee: {}", flux);

        flux.subscribe(System.out::println);
        flux.doOnNext(employee -> LOG.info("em: {}", employee)).subscribe();


    }
}
