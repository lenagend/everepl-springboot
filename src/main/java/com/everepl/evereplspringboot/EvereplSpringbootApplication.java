package com.everepl.evereplspringboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class EvereplSpringbootApplication {

    public static void main(String[] args) {
        SpringApplication.run(EvereplSpringbootApplication.class, args);
    }

}
