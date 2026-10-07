package com.sliit.bookstore.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors(org.springframework.security.config.Customizer.withDefaults()).csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**", "/api/books/**", "/css/**", "/js/**", "/assets/**", "/uploads/**", "/*.html", "/*.css", "/*.js", "/", "/error").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/dashboard/**").permitAll()
                // Customer & Guest Order APIs
                .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/orders/upload-receipt", "/api/orders").permitAll()
                .requestMatchers("/api/orders/my-orders").hasAnyRole("CUSTOMER", "ADMIN", "OWNER")
                .requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/orders/*/receipt").permitAll()
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/orders/{id}").permitAll()
                // Staff role API access
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/orders/**").hasAnyRole("ADMIN","OWNER","OPERATIONS","FINANCE","SUPPORT")
                .requestMatchers("/api/orders/**").hasAnyRole("ADMIN","OPERATIONS","FINANCE")
                .requestMatchers("/api/support/**").hasAnyRole("ADMIN","SUPPORT","CUSTOMER")
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/books/**").hasAnyRole("ADMIN","OWNER","INVENTORY","OPERATIONS","FINANCE","SUPPORT","CUSTOMER")
                .requestMatchers("/api/books/**").hasAnyRole("ADMIN","INVENTORY")
                // Cart and Wishlist only for customers
                .requestMatchers("/cart/**", "/wishlist/**").hasAnyRole("CUSTOMER")
                .requestMatchers("/notifications/**").authenticated()
                .anyRequest().authenticated()
            )
            .exceptionHandling(e -> e.authenticationEntryPoint(
                (request, response, authException) -> response.sendError(401, "Unauthorized")
            ));

        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("authorization", "content-type", "x-auth-token"));
        configuration.setExposedHeaders(Arrays.asList("x-auth-token"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
