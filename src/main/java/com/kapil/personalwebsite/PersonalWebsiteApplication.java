package com.kapil.personalwebsite;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableMongoAuditing
@EnableScheduling
public class PersonalWebsiteApplication {

    public static void main(String[] args) {
        SpringApplication.run(PersonalWebsiteApplication.class, args);
    }

}
