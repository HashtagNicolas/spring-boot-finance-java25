package com.hashtag.ngo.example.bank.api;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

/**
 * Utilisateur de demonstration en memoire (demo / demo123). A remplacer par
 * une veritable gestion des utilisateurs (base de donnees...) hors squelette.
 */
@Configuration
public class UserDetailsConfig {

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        UserDetails demoUser = User.withUsername("demo")
                .password(passwordEncoder.encode("demo123"))
                .roles("USER")
                .build();
        return new InMemoryUserDetailsManager(demoUser);
    }
}
