package cloud.sonam.db.repo;
import cloud.sonam.db.entity.Customer;
import reactor.core.publisher.Flux;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface CustomerRepository extends ReactiveCrudRepository<Customer, Long> {

    @Query("select id, firstname, lastname from customer c where c.lastname = :lastname")
    Flux<Customer> findByLastname(String lastname);
}