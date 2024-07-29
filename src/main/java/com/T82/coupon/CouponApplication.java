package com.T82.coupon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.support.converter.JsonMessageConverter;
import org.springframework.kafka.support.converter.RecordMessageConverter;

@SpringBootApplication
public class CouponApplication {

	public static void main(String[] args) {
		SpringApplication.run(CouponApplication.class, args);
	}

	@Bean
	public RecordMessageConverter converter(){
		return new JsonMessageConverter();
	}

}
