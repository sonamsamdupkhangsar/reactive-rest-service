package cloud.sonam.db.repo;

import cloud.sonam.InfrastructureConfiguration;
import cloud.sonam.db.entity.Customer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Hooks;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = InfrastructureConfiguration.class)
public class CustomerRepositoryIntegrationTests {

    @Autowired
    CustomerRepository customers;
    @Autowired
    DatabaseClient database;

    @Before
    public void setUp() {

        Hooks.onOperatorDebug();

        List<String> statements = Arrays.asList(//
                "DROP TABLE IF EXISTS customer;",
                "CREATE TABLE customer ( id SERIAL PRIMARY KEY, firstname VARCHAR(100) NOT NULL, lastname VARCHAR(100) NOT NULL);");

        statements.forEach(it -> database.sql(it) //
                .fetch() //
                .rowsUpdated() //
                .as(StepVerifier::create) //
                .expectNextCount(1) //
                .verifyComplete());
    }

    @Test
    public void executesFindAll() throws IOException {

        Customer dave = new Customer(null, "Dave", "Matthews");
        Customer carter = new Customer(null, "Carter", "Beauford");

        insertCustomers(dave, carter);

        customers.findAll() //
                .as(StepVerifier::create) //
                .assertNext(dave::equals) //
                .assertNext(carter::equals) //
                .verifyComplete();
    }

    @Test
    public void executesAnnotatedQuery() throws IOException {

        Customer dave = new Customer(null, "Dave", "Matthews");
        Customer carter = new Customer(null, "Carter", "Beauford");

        insertCustomers(dave, carter);

        customers.findByLastname("Matthews") //
                .as(StepVerifier::create) //
                .assertNext(dave::equals) //
                .verifyComplete();
    }

    private void insertCustomers(Customer... customers) {

        this.customers.saveAll(Arrays.asList(customers))//
                .as(StepVerifier::create) //
                .expectNextCount(2) //
                .verifyComplete();
    }
}