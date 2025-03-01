package com.kinjo.Beauthrist_Backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BeauthristBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BeauthristBackendApplication.class, args);
	}
}
