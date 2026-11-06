package com.sugarpop.loyalty.config;

import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(Customizer.withDefaults())
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/clientes", "/api/clientes/login-cliente")
            )
            .authorizeHttpRequests(auth -> auth
                // Permitir preflight CORS (OPTIONS) desde la PWA en Netlify
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // Páginas y recursos públicos
                .requestMatchers(
                    "/",
                    "/registro",
                    "/images/**",
                    "/css/**",
                    "/js/**",
                    "/favicon.ico"
                ).permitAll()

                // El registro, el login de cliente (teléfono + email) y el QR son públicos para la PWA
                .requestMatchers(HttpMethod.POST, "/api/clientes", "/api/clientes/login-cliente").permitAll()
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/clientes/*/qr",
                    "/api/clientes/*/qr/descargar"
                ).permitAll()

                // POS y las APIs que utiliza: requieren autenticación del cajero
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

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of(
            "https://*.netlify.app",
            "https://*.vercel.app",
            "http://localhost:*",
            "http://192.168.*:*"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}
