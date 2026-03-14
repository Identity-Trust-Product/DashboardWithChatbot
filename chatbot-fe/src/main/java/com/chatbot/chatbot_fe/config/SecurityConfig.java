package com.chatbot.chatbot_fe.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for chatbot-fe application.
 *
 * This config allows all requests through without authentication
 * and disables CSRF for simplicity.
 *
 * To enable full login, comment out the "permitAll" line and
 * uncomment the formLogin block below.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            .authorizeHttpRequests(auth -> auth
                // ---- DEV MODE: allow everything ----
                .anyRequest().permitAll()

                // ---- PRODUCTION MODE (uncomment when ready) ----
                // .requestMatchers(
                //     "/css/**", "/js/**", "/images/**",
                //     "/webjars/**", "/common/**",
                //     "/login", "/error", "/",
                //     "/get-fe-service-version",
                //     "/get-be-service-version"
                // ).permitAll()
                // .anyRequest().authenticated()
            )
            .csrf(csrf -> csrf
                // Disable CSRF for REST API endpoints if needed
                // For full CSRF protection, remove this .disable() line
                .disable()
            )
            .formLogin(form -> form.disable())   // Remove when using login form
            .httpBasic(basic -> basic.disable()); // Remove when using basic auth

        // ---- To use Spring's default login page instead, replace above with: ----
        // .formLogin(form -> form
        //     .loginPage("/login")
        //     .loginProcessingUrl("/login")
        //     .defaultSuccessUrl("/", true)
        //     .failureUrl("/login?error=true")
        //     .permitAll()
        // )
        // .logout(logout -> logout
        //     .logoutUrl("/logout")
        //     .logoutSuccessUrl("/login")
        //     .permitAll()
        // );

        return http.build();
    }
}