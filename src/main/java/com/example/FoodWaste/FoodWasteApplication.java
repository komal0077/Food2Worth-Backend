package com.example.FoodWaste;

import org.flywaydb.core.Flyway;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FoodWasteApplication {

	public static void main(String[] args) {
		// Spring Boot 4.1.0-RC1 does not ship a Flyway autoconfiguration module yet,
		// so migrations are run explicitly here before the context (and Hibernate's
		// schema validation) starts up.
		String dbPassword = System.getenv("DB_PASSWORD");

		if (dbPassword == null || dbPassword.isBlank()) {
			throw new IllegalStateException("DB_PASSWORD environment variable must be set");
		}

		Flyway.configure()
				.dataSource(
						resolve("DB_URL", "jdbc:mysql://localhost:3306/FoodWaste"),
						resolve("DB_USERNAME", "root"),
						dbPassword)
				.baselineOnMigrate(Boolean.parseBoolean(resolve("FLYWAY_BASELINE_ON_MIGRATE", "false")))
				.baselineVersion("0")
				.load()
				.migrate();

		SpringApplication.run(FoodWasteApplication.class, args);
	}

	private static String resolve(String envVar, String defaultValue) {
		String value = System.getenv(envVar);
		return value != null ? value : defaultValue;
	}

}
