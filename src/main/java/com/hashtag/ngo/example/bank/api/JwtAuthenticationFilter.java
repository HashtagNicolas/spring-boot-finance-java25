package com.hashtag.ngo.example.bank.api;

import com.hashtag.ngo.example.bank.bean.AuditContext;
import com.hashtag.ngo.example.bank.bean.AuditContextHolder;
import com.hashtag.ngo.example.bank.bean.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Filtre d'authentification JWT : lit l'en-tete "Authorization: Bearer
 * &lt;jeton&gt;", le valide via JwtService, et peuple le SecurityContext si
 * valide. S'execute une seule fois par requete (OncePerRequestFilter), avant
 * que Spring Security n'evalue les regles authorizeHttpRequests.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (header != null && header.startsWith(BEARER_PREFIX)) {
            String token = header.substring(BEARER_PREFIX.length());
            if (jwtService.validateToken(token)) {
                String username = jwtService.extractSubject(token);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(username, null, List.of());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);

                // Scoped Values (JEP 506) : on propage le contexte d'audit
                // (utilisateur authentifie + identifiant de requete) pour
                // TOUTE la suite du traitement de cette requete HTTP, c'est-a-
                // dire l'execution de filterChain.doFilter(...) ci-dessous
                // (qui enchaine les filtres suivants PUIS l'appel au
                // controleur/service). N'importe quel code appele
                // transitivement (ex : TransactionServiceImpl#recordTransaction)
                // peut alors lire AuditContextHolder.CURRENT pour tracer "qui"
                // a effectue l'operation, sans que ce contexte transite
                // explicitement en parametre de chaque methode traversee.
                // La liaison est un bloc structure : elle n'existe que le
                // temps de l'appel run(...) ci-dessous et disparait
                // automatiquement a la fin de la requete - contrairement a un
                // ThreadLocal, pas de remove() a oublier, donc pas de risque
                // de fuite du contexte d'une requete vers la suivante sur un
                // thread reutilise (pool de threads du serveur).
                String requestId = UUID.randomUUID().toString();
                AuditContext auditContext = new AuditContext(username, requestId);
                doFilterWithAuditContext(auditContext, request, response, filterChain);
                return;
            }
            // Jeton absent ou invalide : on ne leve rien ici, on laisse le
            // SecurityContext vide. C'est ensuite authorizeHttpRequests +
            // l'authenticationEntryPoint (401) qui rejettent la requete si
            // l'endpoint appele exige d'etre authentifie.
        }

        filterChain.doFilter(request, response);
    }

    /**
     * ScopedValue.Carrier#run(Runnable) n'accepte qu'un java.lang.Runnable,
     * qui ne peut pas declarer ServletException/IOException. On capture donc
     * ces exceptions checked dans une RuntimeException intermediaire, pour
     * les relancer telles quelles juste apres etre sortis du bloc ScopedValue.
     */
    private void doFilterWithAuditContext(AuditContext auditContext,
                                           HttpServletRequest request,
                                           HttpServletResponse response,
                                           FilterChain filterChain) throws ServletException, IOException {
        try {
            ScopedValue.where(AuditContextHolder.CURRENT, auditContext).run(() -> {
                try {
                    filterChain.doFilter(request, response);
                } catch (IOException | ServletException e) {
                    throw new FilterChainExecutionException(e);
                }
            });
        } catch (FilterChainExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof IOException io) {
                throw io;
            }
            if (cause instanceof ServletException se) {
                throw se;
            }
            throw e;
        }
    }

    /** Enveloppe technique privee, voir {@link #doFilterWithAuditContext}. */
    private static final class FilterChainExecutionException extends RuntimeException {
        FilterChainExecutionException(Exception cause) {
            super(cause);
        }
    }
}
