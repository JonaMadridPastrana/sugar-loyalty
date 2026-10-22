package com.sugarpop.loyalty;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// @SpringBootApplication: Le dice a Spring que este es el punto de entrada de la app
// @EnableScheduling: Activa el motor de tareas programadas.
//   Sin esta anotación, los @Scheduled que agreguemos más adelante
//   serían ignorados completamente.
@EnableScheduling
@SpringBootApplication
public class SugarLoyaltyApplication {

    public static void main(String[] args) {
        SpringApplication.run(SugarLoyaltyApplication.class, args);
    }

}
