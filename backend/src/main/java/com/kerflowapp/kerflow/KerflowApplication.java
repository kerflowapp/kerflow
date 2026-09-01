package com.kerflowapp.kerflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@Import({BaseConfiguration.class})
@EnableScheduling
public class KerflowApplication {

    public static void main(String[] args) {
        SpringApplication.run(KerflowApplication.class, args);
    }

}
