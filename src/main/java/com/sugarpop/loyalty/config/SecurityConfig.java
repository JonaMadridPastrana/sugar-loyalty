package com.sugarpop.loyalty.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // Páginas y recursos públicos
                .requestMatchers(
                    "/",
                    "/registro",
                    "/images/**",
                    "/css/**",
                    "/js/**",
                    "/favicon.ico"
                ).permitAll()

                // El registro y el QR deben seguir disponibles sin iniciar sesión
                .requestMatchers(HttpMethod.POST, "/api/clientes").permitAll()
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/clientes/*/qr",
                    "/api/clientes/*/qr/descargar"
                ).permitAll()

                // POS y las APIs que utiliza: requieren autenticación
                .requestMatchers(
                    "/pos",
                    "/api/transacciones/**",
                    "/api/recompensas/**",
                    "/api/clientes/**"
                ).authenticated()

                // Protege también las futuras rutas API por defecto
                .requestMatchers("/api/**").authenticated()

                // Las rutas que no son API y no fueron especificadas quedan públicas
                .anyRequest().permitAll()
            )
            .formLogin(Customizer.withDefaults());

        return http.build();
    }
}