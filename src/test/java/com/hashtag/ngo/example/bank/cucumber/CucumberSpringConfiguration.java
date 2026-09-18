package com.hashtag.ngo.example.bank.cucumber;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Point de branchement entre Cucumber et le contexte Spring Boot :
 * @CucumberContextConfiguration indique a cucumber-spring d'utiliser cette
 * classe pour demarrer/partager le contexte Spring entre tous les scenarios.
 * @SpringBootTest(webEnvironment = RANDOM_PORT) demarre l'application
 * complete (securite, JPA/H2, MVC...) sur un port HTTP libre, expose ensuite
 * aux step definitions via @Value("${local.server.port}").
 * <p>
 * Cette classe n'a pas besoin de code : sa seule presence, avec ces
 * annotations, suffit a cucumber-spring pour construire un unique contexte
 * Spring reutilise par tous les scenarios de la suite (plus rapide qu'un
 * demarrage par scenario).
 */
@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CucumberSpringConfiguration {
}
