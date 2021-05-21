package cloud.sonam.service;

import cloud.sonam.db.entity.Customer;
import cloud.sonam.db.repo.CustomerRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class TransactionalService {

    private final @NonNull CustomerRepository repository;

    /**
     * Saves the given {@link Customer} unless its firstname is "Dave".
     *
     * @param customer must not be {@literal null}.
     * @return
     */
    @Transactional
    public Mono<Customer> save(Customer customer) {

        return repository.save(customer).map(it -> {

            if (it.getFirstname().equals("Dave")) {
                throw new IllegalStateException();
            } else {
                return it;
            }
        });
    }
}