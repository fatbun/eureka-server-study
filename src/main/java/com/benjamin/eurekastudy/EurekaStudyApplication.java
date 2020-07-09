package com.benjamin.eurekastudy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@EnableEurekaServer
@SpringBootApplication
public class EurekaStudyApplication {

    public static void main(String[] args) {
        SpringApplication.run(EurekaStudyApplication.class,
                args);
    }

}
