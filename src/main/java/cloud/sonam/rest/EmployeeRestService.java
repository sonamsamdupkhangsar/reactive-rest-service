package cloud.sonam.rest;

import cloud.sonam.db.entity.Employee;
import cloud.sonam.db.service.impl.EmployeeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private EmployeeService employeeService;

    public EmployeeRestService() {
        LOG.info("got constructed");
        System.out.println("hello");
    }

    @GetMapping
    public Mono<Employee> hello() {
        LOG.info("helloo");
        return Mono.just(new Employee(1l, "Helloo"));
    }
    @GetMapping("/id/{id}")
    public Mono<Employee> getEmployeeById(@PathVariable Long id) {
        LOG.info("get employee by id: {}", id);
        return employeeService.getById(id);
    }

    @GetMapping("/name/{name}")
    public Flux<Employee> getEmployeeByFirstName(@PathVariable String name) {
        List<Employee> employeeList = new ArrayList<>();
        LOG.info("get employees by name: {}", name);

        return employeeService.getByName(name);
    }

    @GetMapping("/resp/name/{name}")
    public Flux<ResponseEntity<List<Employee>>> getEmployeeByNameRes(@PathVariable String name) {
        List<Employee> employeeList = new ArrayList<>();
        LOG.info("get employees by name: {}", name);

        employeeList.add(new Employee(1L, "sonam tashi"));
        employeeList.add(new Employee(2L, "sonam lama"));
        employeeList.add(new Employee(3L, "sonam nepali"));

        return Flux.just(new ResponseEntity<>(employeeList, HttpStatus.OK));
    }

    @PostMapping
    private Mono<Employee> update(@RequestBody Employee employee) {
       LOG.info("saving employee request: {}", employee);
       return employeeService.save(employee);
    }

}
