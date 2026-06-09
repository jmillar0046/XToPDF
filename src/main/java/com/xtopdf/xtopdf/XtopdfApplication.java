package com.xtopdf.xtopdf;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class XtopdfApplication {

	public static void main(String[] args) {
		SpringApplication.run(XtopdfApplication.class, args);
	}

}
