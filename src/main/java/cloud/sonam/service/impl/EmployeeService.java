package cloud.sonam.service.impl;

import cloud.sonam.db.entity.Employee;
import cloud.sonam.db.repo.EmployeeRepository;
import cloud.sonam.service.EmployeeManage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;

@Service
public class EmployeeService implements EmployeeManage {
    private static final Logger LOG = LoggerFactory.getLogger(EmployeeService.class);

    @Autowired
    private EmployeeRepository employeeRepository;

    @Override
    public Mono<Employee> save(Employee employee) {
        Mono<Employee> mono = employeeRepository.save(employee);
        mono.log();

        return mono;
    }

    @Override
    public Flux<Employee> saveAll(Employee... employees) {
        return employeeRepository.saveAll(Arrays.asList(employees));
    }

    @Override
    public Flux<Employee> getAll() {
        LOG.info("get all employee");
        return employeeRepository.findAll();
    }

    @Override
    public Flux<Employee> getByName(String name) {
        return employeeRepository.findByName(name);
    }

    @Override
    public Mono<Employee> getById(long id) {
        return employeeRepository.findById(id);
    }

    @Override
    public Mono<Void> delete(Employee employee) {
        LOG.info("deleting employee: {}", employee);
        return employeeRepository.delete(employee);
    }
}
