package com.marnone.ai_arena;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class AiArenaApplication {

	public static void main(String[] args) {
		SpringApplication.run(AiArenaApplication.class, args);
	}

}
