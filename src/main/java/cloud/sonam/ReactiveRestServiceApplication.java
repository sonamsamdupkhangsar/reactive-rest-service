package cloud.sonam;

import cloud.sonam.db.entity.Customer;
import cloud.sonam.reactiverestservice.EmployeeWebClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.r2dbc.spi.ConnectionFactory;
import reactor.core.publisher.Mono;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.r2dbc.mapping.event.BeforeConvertCallback;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator;
import org.springframework.r2dbc.core.DatabaseClient;

@SpringBootApplication
public class ReactiveRestServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReactiveRestServiceApplication.class, args);
	}
	@Bean
	BeforeConvertCallback<Customer> idGeneratingCallback(DatabaseClient databaseClient) {

		return (customer, sqlIdentifier) -> {

			if (customer.getId() == null) {

				return databaseClient.sql("SELECT primary_key.nextval") //
						.map(row -> row.get(0, Long.class)) //
						.first() //
						.map(customer::withId);
			}

			return Mono.just(customer);
		};
	}

	@Bean
	ConnectionFactoryInitializer initializer(ConnectionFactory connectionFactory) {

		ConnectionFactoryInitializer initializer = new ConnectionFactoryInitializer();
		initializer.setConnectionFactory(connectionFactory);
		initializer.setDatabasePopulator(new ResourceDatabasePopulator(new ByteArrayResource(("CREATE SEQUENCE primary_key;"
				+ "DROP TABLE IF EXISTS customer;"
				+ "CREATE TABLE customer (id INT PRIMARY KEY, firstname VARCHAR(100) NOT NULL, lastname VARCHAR(100) NOT NULL);"
				+ "DROP TABLE IF EXISTS employee;"
				+ "CREATE TABLE employee (id SERIAL PRIMARY KEY, name VARCHAR(100) NOT NULL);")
				.getBytes())));


		return initializer;
	}
}
