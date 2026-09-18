package com.hashtag.ngo.example.bank.api;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuration Spring Security : API REST stateless authentifiee par JWT
 * (pas de session HTTP, pas de formulaire de connexion classique).
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF protege les formulaires HTML bases sur cookies/session ;
                // une API REST stateless authentifiee par jeton Bearer n'y est
                // pas exposee, on peut donc le desactiver sans risque.
                .csrf(csrf -> csrf.disable())
                // Aucune HttpSession creee/utilisee : l'authentification est
                // reconstruite a chaque requete a partir du jeton JWT
                // (JwtAuthenticationFilter), jamais stockee cote serveur.
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/auth/token", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/accounts/**", "/transactions/**").authenticated()
                        .anyRequest().authenticated())
                // Sans ce point d'entree explicite, Spring Security renverrait
                // 403 (Forbidden) pour une requete non authentifiee (jeton
                // absent OU invalide), ce qui est trompeur : 403 signifie
                // normalement "authentifie mais pas autorise". Avec
                // HttpStatusEntryPoint(UNAUTHORIZED), toute requete qui n'a pas
                // reussi a s'authentifier recoit bien un 401.
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                // Notre filtre JWT s'execute avant le filtre standard de
                // connexion par formulaire (non utilise ici, mais reste le
                // point de repere conventionnel pour se positionner tot dans
                // la chaine de filtres de securite).
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /**
     * Expose l'AuthenticationManager par defaut de Spring Security (construit
     * a partir du UserDetailsService et du PasswordEncoder ci-dessous), pour
     * qu'AuthController puisse authentifier un couple username/password.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
