package com.starting;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class StartingApplication {

	public static void main(String[] args) {
		SpringApplication.run(StartingApplication.class, args);
	}

}