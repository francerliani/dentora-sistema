package com.dentora.dentora.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. QUIÉN puede entrar A DÓNDE
                .authorizeHttpRequests(auth -> auth
                        // El login, el CSS y las imágenes se ven SIN iniciar sesión
                        // (si no, la pantalla de login se vería sin estilos ni logo)
                        .requestMatchers("/login", "/error", "/css/**", "/img/**").permitAll()

                        // Pacientes y Turnos: solo Secretaria y Administrador
                        // (/** significa "esta página y todo lo que cuelga": /pacientes/nuevo,
                        // /pacientes/editar/3, etc.)
                        .requestMatchers("/pacientes/**", "/turnos/**").hasAnyRole("SECRETARIA", "ADMINISTRADOR")

                        // Odontograma: solo Odontólogo y Administrador
                        .requestMatchers("/odontograma/**").hasAnyRole("ODONTOLOGO", "ADMINISTRADOR")

                        // Configuración: solo Administrador
                        .requestMatchers("/configuracion/**").hasRole("ADMINISTRADOR")

                        .anyRequest().authenticated())
                // 2. El LOGIN
                .formLogin(form -> form
                        .loginPage("/login") // usar TU pantalla, no la de Spring
                        .defaultSuccessUrl("/inicio", true) // si el login sale bien, ir a /inicio
                        .permitAll())
                // 3. El CIERRE DE SESIÓN
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout") // al cerrar sesión, volver al login
                        .permitAll());

        return http.build();
    }
}