package cloud.sonam;

import cloud.sonam.db.entity.Customer;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.r2dbc.mapping.event.BeforeConvertCallback;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.reactive.config.CorsRegistry;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Collections;

@SpringBootApplication
public class ReactiveRestServiceApplication {

	@Value("${trustOrigin}")
	private String trustOrigin;

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
				+ "CREATE TABLE employee (id SERIAL PRIMARY KEY, name VARCHAR(100) NOT NULL);"
				+ "DROP TABLE IF EXISTS video;"
				+ "CREATE TABLE video ( id SERIAL PRIMARY KEY, name varchar(255),  thumb varchar(255), path VARCHAR(255) NOT NULL, stored datetime NOT NULL);"
		)
				.getBytes())));


		return initializer;
	}
	@Bean//ClassPathResource
	public RouterFunction<ServerResponse> imgRouter() {
		return RouterFunctions
				.resources("/**", new FileSystemResource("/Users/ssamdupk/Documents/bitbucket/reactive-rest-service/videofiles"));
	}

	/*@Bean
	public FilterRegistrationBean<CorsFilter> simpleCorsFilter() {
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowCredentials(true);

		config.setAllowedOrigins(Collections.singletonList(trustOrigin));
		config.setAllowedMethods(Collections.singletonList("*"));
		config.setAllowedHeaders(Collections.singletonList("*"));
		source.registerCorsConfiguration("/**", config);


		FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<>(new CorsFilter(source));
		bean.setOrder(Ordered.HIGHEST_PRECEDENCE);

		return bean;
	}*/

	@Configuration
	@EnableWebFlux
	public class CorsGlobalConfiguration implements WebFluxConfigurer {

		@Override
		public void addCorsMappings(CorsRegistry corsRegistry) {
			corsRegistry.addMapping("/**")
					.allowedOrigins(trustOrigin)
					.allowedMethods("GET", "POST", "PUT")
					.maxAge(3600);
		}
	}
}
