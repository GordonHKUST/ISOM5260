package com.hkust.isom5260;

import com.hkust.isom5260.dto.Car;
import com.hkust.isom5260.mapper.CarMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@SpringBootApplication
public class MybatisDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(MybatisDemoApplication.class, args);
	}


	@Bean
    CommandLineRunner demo (CarMapper carMapper) {
	    return args -> {

            List<Car> cars = Arrays.asList(
                    new Car("Honda", "Civic", 1984, null),
                    new Car("BMW", "330i", 2012, null),
                    new Car("Infiniti", "Q50", 2014, null)
                    );
            for(int i = 0 ; i < 10; i++) {
                cars.forEach(car -> {
                    Collection<Car> car2 = carMapper.selectAll();
                   // if (CollectionUtils.isEmpty(car2)) {
                        carMapper.insert(car);
                   // }
                    System.out.println(car.toString());
                });
            }
        };
    }


}



