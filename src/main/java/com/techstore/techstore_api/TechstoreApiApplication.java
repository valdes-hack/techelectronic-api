package com.techstore.techstore_api;

import org.springframework.boot.SpringApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class TechstoreApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(TechstoreApiApplication.class, args);
	}

}
