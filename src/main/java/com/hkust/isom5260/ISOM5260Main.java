package com.hkust.isom5260;

import com.hkust.isom5260.dto.Car;
import com.hkust.isom5260.mapper.CarMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@SpringBootApplication
@EnableScheduling
public class ISOM5260Main {
	public static void main(String[] args) {
		SpringApplication.run(ISOM5260Main.class, args);
	}
}



