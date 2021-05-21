package cloud.sonam.service;

import cloud.sonam.db.entity.Employee;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface EmployeeManage {
    Mono<Employee> save(Employee employee);

    Flux<Employee> saveAll(Employee... employees);

    Flux<Employee> getAll();

    Flux<Employee> getByName(String name);

    Mono<Employee> getById(long id);

    Mono<Void> delete(Employee employee);
}
