package com.example.stationsapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;

@SpringBootApplication
public class StationsApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(StationsApiApplication.class, args);
    }
}
