package com.proyecto.fundaciondeportiva.config;

import com.proyecto.fundaciondeportiva.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // ⭐ CRÍTICO: Ignorar rutas públicas COMPLETAMENTE
        String path = request.getRequestURI();
        if (path.startsWith("/api/auth/") || path.startsWith("/api/usuarios/crear")) {
            filterChain.doFilter(request, response);
            return; // ⭐ NO procesar JWT para estas rutas
        }

        // ⭐ Saltar OPTIONS (preflight) sin validar token
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        // ✅ BUSCAR TOKEN PRIMERO EN HEADER, LUEGO EN COOKIES
        String jwt = null;

        // 1. Intentar obtener de header Authorization
        final String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7);
        }

        // 2. Si no está en header, intentar en cookie
        if (jwt == null) {
            jwt = getJwtFromCookies(request);
        }

        // 3. Si no hay token en ningún lado, continuar sin autenticar
        if (jwt == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 4. Extraer email del token
            String userEmail = jwtService.extractUsername(jwt);

            // 5. Si hay email y no está autenticado aún
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

                // 6. Validar token
                if (jwtService.validateToken(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // Solo loguear, NO bloquear la petición
            logger.warn("Error al procesar el token JWT: " + e.getMessage());
        }

        // SIEMPRE continuar con el chain
        filterChain.doFilter(request, response);
    }

    private String getJwtFromCookies(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        return Arrays.stream(cookies)
                .filter(cookie -> "jwt_token".equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }
}