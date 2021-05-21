package cloud.sonam;

import cloud.sonam.db.entity.Customer;
import cloud.sonam.db.repo.CustomerRepository;
import cloud.sonam.service.TransactionalService;
import reactor.core.publisher.Hooks;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Integration tests for {@link TransactionalService}.
 *
 * @author Oliver Drotbohm
 * @soundtrack Shame - Tedeschi Trucks Band (Signs)
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = InfrastructureConfiguration.class)
public class TransactionalServiceIntegrationTests {
    @Autowired
    TransactionalService transactionalService;
    @Autowired
    CustomerRepository customerRepository;
    @Autowired DatabaseClient database;

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

    @Test // #500
    public void exceptionTriggersRollback() {

        transactionalService.save(new Customer(null, "Dave", "Matthews")) //
                .as(StepVerifier::create) //
                .expectError() // Error because of the exception triggered within the service
                .verify();

        // No data inserted due to rollback
        customerRepository.findByLastname("Matthews") //
                .as(StepVerifier::create) //
                .verifyComplete();
    }

    @Test // #500
    public void insertsDataTransactionally() {

        transactionalService.save(new Customer(null, "Carter", "Beauford")) //
                .as(StepVerifier::create) //
                .expectNextMatches(Customer::hasId) //
                .verifyComplete();

        // Data inserted due to commit
        customerRepository.findByLastname("Beauford") //
                .as(StepVerifier::create) //
                .expectNextMatches(Customer::hasId) //
                .verifyComplete();
    }}
