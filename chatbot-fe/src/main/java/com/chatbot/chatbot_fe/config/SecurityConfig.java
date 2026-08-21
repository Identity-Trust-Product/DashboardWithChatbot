package com.chatbot.chatbot_fe.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final RemoteAuthenticationProvider authenticationProvider;

    public SecurityConfig(RemoteAuthenticationProvider authenticationProvider) {
        this.authenticationProvider = authenticationProvider;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            .authenticationProvider(authenticationProvider)
            .authorizeHttpRequests(auth -> auth
                // Allow static resources and login page without authentication
                .requestMatchers(
                    "/login", "/login/**", "/register", "/register/**",
                    "/css/**", "/js/**", "/images/**",
                    "/webjars/**", "/favicon.ico",
                    "/error"
                ).permitAll()
                // Everything else requires login
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")              // your custom login page GET /login
                .loginProcessingUrl("/login")     // Spring handles POST /login
                .defaultSuccessUrl("/", true)     // redirect to dashboard after login
                .failureUrl("/login?error=true")  // back to login on wrong password
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .permitAll()
            )
            .headers(headers -> headers
            .frameOptions(frame -> frame.sameOrigin())
            )
            .csrf(csrf -> csrf.disable()); // keep disabled for REST endpoints

        return http.build();
    }
}

