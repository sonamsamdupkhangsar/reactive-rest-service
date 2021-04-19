package cloud.sonam.service;

import cloud.sonam.InfrastructureConfiguration;
import cloud.sonam.db.entity.Employee;
import cloud.sonam.db.service.impl.EmployeeService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Hooks;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {InfrastructureConfiguration.class, EmployeeService.class})
public class EmployeeServiceIntegTests {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    DatabaseClient database;

    @Before
    public void setUp() {

        Hooks.onOperatorDebug();

        List<String> statements = Arrays.asList(//
                "DROP TABLE IF EXISTS employee;",
                "CREATE TABLE employee ( id SERIAL PRIMARY KEY, name VARCHAR(100) NOT NULL);");

        statements.forEach(it -> database.sql(it) //
                .fetch() //
                .rowsUpdated() //
                .as(StepVerifier::create) //
                .expectNextCount(1) //
                .verifyComplete());
    }

    @Test
    public void save() {
        Employee sonam = new Employee(null, "Sonam Wangyal");
        Employee tashi = new Employee(null, "Tashi Jamuna");
        employeeService.saveAll(sonam, tashi)
                .log()
                .as(StepVerifier::create)
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    public void delete() {
        Employee emp1 = new Employee(null, "Red spice");
        Employee emp2 = new Employee(null, "Green Jamuna");
        employeeService.saveAll(emp1, emp2)
                .log()
                .as(StepVerifier::create)
                .expectNextCount(2)
                .verifyComplete();

        Flux<Employee> employeeFlux = employeeService.getByName("Red spice");

        employeeFlux.as(StepVerifier::create)
                .expectNextCount(1)
                .verifyComplete();

        employeeFlux = employeeService.getByName("Red spice");
        employeeFlux
                .flatMap(employeeService::delete)
                .as(StepVerifier::create)
                .verifyComplete();


       employeeService.getAll()
                .log()
                .as(StepVerifier::create)
                .expectNext(emp2)
                .verifyComplete();
    }


}
