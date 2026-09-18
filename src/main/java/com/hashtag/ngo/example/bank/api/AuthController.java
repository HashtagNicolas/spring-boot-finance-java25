package com.hashtag.ngo.example.bank.api;

import com.hashtag.ngo.example.bank.bean.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Authentification : echange {username, password} contre un jeton JWT.
 */
@RestController
@RequestMapping("/auth")
@Tag(name = "Authentification", description = "Emission de jetons JWT")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @PostMapping("/token")
    @SecurityRequirements // endpoint public : n'affiche pas le cadenas "Bearer" dans Swagger UI
    @Operation(summary = "Authentifie un utilisateur et renvoie un jeton JWT")
    public TokenResponse token(@RequestBody AuthRequest request) {
        // AuthenticationManager delegue a UserDetailsConfig (utilisateur
        // demo/demo123) + au PasswordEncoder. En cas d'identifiants invalides,
        // il leve une AuthenticationException qui remonte jusqu'au filtre de
        // securite : c'est le authenticationEntryPoint configure dans
        // SecurityConfig (401) qui la traduit en reponse HTTP, pas
        // GlobalExceptionHandler.
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        return new TokenResponse(jwtService.generateToken(authentication.getName()));
    }
}
