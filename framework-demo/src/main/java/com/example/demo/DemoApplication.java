package com.example.demo;

import io.github.penn.rest.EnableRestService;
import io.github.penn.rest.EnableWebContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableRestService(serviceBasePackage = "com.example.demo.rest.service")
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

}
