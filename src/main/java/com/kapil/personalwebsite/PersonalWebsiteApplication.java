package com.kapil.personalwebsite;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@SpringBootApplication
@EnableMongoAuditing
public class PersonalWebsiteApplication {

    public static void main(String[] args) {
        SpringApplication.run(PersonalWebsiteApplication.class, args);
    }

}
