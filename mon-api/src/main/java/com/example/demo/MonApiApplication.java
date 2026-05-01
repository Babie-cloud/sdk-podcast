package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication(scanBasePackages = {"com.example.demo", "com.ngpodcast"})
@EnableJpaRepositories(basePackages = "com.ngpodcast")
@EntityScan(basePackages = "com.ngpodcast")
public class MonApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(MonApiApplication.class, args);
    }
}
