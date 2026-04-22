package edu.tcu.cs.projectpulse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ProjectpulseApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProjectpulseApplication.class, args);
    }

}
