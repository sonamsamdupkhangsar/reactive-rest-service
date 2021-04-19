package cloud.sonam.rest;

import cloud.sonam.db.entity.Employee;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/employees")
public class EmployeeRestService {
    private static final Logger LOG = LoggerFactory.getLogger(EmployeeRestService.class);

    public EmployeeRestService() {
        LOG.info("got constructed");
        System.out.println("hello");
    }

    @GetMapping("/id/{id}")
    public Mono<Employee> getEmployeeById(@PathVariable Long id) {
        LOG.info("get employee by id: {}", id);
        return Mono.just(new Employee(id, "sonam"));
    }

    @GetMapping("/name/{firstName}")
    public Flux<Employee> getEmployeeByFirstName(@PathVariable String firstName) {
        List<Employee> employeeList = new ArrayList<>();
        LOG.info("get employees by name: {}", firstName);

        var emp1 = new Employee(1L, "sonam tashi");
        Flux flux = Flux.just(emp1, new Employee(2L, "sonam lama"),
                new Employee(3L, "sonam nepali"));

        flux.log();
        return flux;
    }

    @GetMapping("/resp/name/{fName}")
    public Flux<ResponseEntity<List<Employee>>> getEmployeeByFirstNameRes(@PathVariable String fName) {
        List<Employee> employeeList = new ArrayList<>();
        LOG.info("get employees by name: {}", fName);

        employeeList.add(new Employee(1L, "sonam tashi"));
        employeeList.add(new Employee(2L, "sonam lama"));
        employeeList.add(new Employee(3L, "sonam nepali"));

        return Flux.just(new ResponseEntity<>(employeeList, HttpStatus.OK));
    }

    @PostMapping
    private Mono<ResponseEntity<Employee>> update(@RequestBody Employee employee) {
        return Mono.just(new ResponseEntity<>(new Employee(employee.getId(), employee.getName()), HttpStatus.OK));
    }

}
