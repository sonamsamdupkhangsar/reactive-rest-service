package cloud.sonam.db.repo;

import cloud.sonam.db.entity.Customer;
import cloud.sonam.db.entity.Employee;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;


public interface EmployeeRepository extends ReactiveCrudRepository<Employee, Long> {

    @Query("select id, name from employee e where e.name = :name")
    Flux<Employee> findByName(String name);
}
