package com.javasampleapproach.springrest.mysql;

import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SpringRestMySqlApplication {

	@Bean
	public ModelMapper modelMapper() {
		return new ModelMapper();
	}


	// most hattricks per player
	// most penalty goals per player
	// penalty goals for each team
	// hattricks per team
	// goly obrancov, zaloznikov, utocnikov
	// vlastne goly
	// zobrazenie zapasov hraca v ktorych dal gol, dostal kartu, vlastny gol atd
	// longest unbeaten run
	public static void main(String[] args) {
		SpringApplication.run(SpringRestMySqlApplication.class, args);
	}
}
