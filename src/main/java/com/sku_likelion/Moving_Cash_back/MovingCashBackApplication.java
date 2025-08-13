package com.sku_likelion.Moving_Cash_back;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class MovingCashBackApplication {

	public static void main(String[] args) {
		SpringApplication.run(MovingCashBackApplication.class, args);
	}

}
