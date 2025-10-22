package com.example.orzon_example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class OrzonExampleApplication {

	public static void main(String[] args) {
		SpringApplication.run(OrzonExampleApplication.class, args);
	}

}
