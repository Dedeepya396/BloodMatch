package com.example.bloodmatch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BloodmatchApplication {

	public static void main(String[] args) {
		SpringApplication.run(BloodmatchApplication.class, args);
	}

}
