package com.dentora.dentora;

import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DentoraApplication {

    public static void main(String[] args) {
        // Fija la zona horaria con el nombre que PostgreSQL acepta.
        // Tiene que ir ANTES de SpringApplication.run, porque la conexión
        // a la base se abre durante el arranque.
        TimeZone.setDefault(TimeZone.getTimeZone("America/Argentina/Buenos_Aires"));

        SpringApplication.run(DentoraApplication.class, args);
    }
}