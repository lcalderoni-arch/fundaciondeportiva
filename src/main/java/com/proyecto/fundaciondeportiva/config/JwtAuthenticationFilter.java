package com.proyecto.fundaciondeportiva.config;

import com.proyecto.fundaciondeportiva.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull; // Para indicar que los parámetros no deben ser nulos
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

@Component // Para que Spring lo detecte como un bean
public class JwtAuthenticationFilter extends OncePerRequestFilter { // Se ejecuta una vez por petición

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserDetailsService userDetailsService; // Spring inyectará nuestro UsuarioService aquí

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        
        final String jwt = getJwtFromCookies(request);
        final String userEmail;

        // 1. Verifica si viene el header 'Authorization' y si empieza con 'Bearer '
        if (jwt == null) {
            filterChain.doFilter(request, response); // Si no hay token, pasa al siguiente filtro
            return;
        }

        try {
            // 3. Extrae el email del token usando JwtService
            userEmail = jwtService.extractUsername(jwt);

            // 4. Verifica si el email no es nulo y si el usuario aún no está autenticado
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // 5. Carga los detalles del usuario desde la base de datos usando UserDetailsService
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

                // 6. Valida el token usando JwtService (comprueba email y expiración)
                if (jwtService.validateToken(jwt, userDetails)) {
                    // 7. Si el token es válido, crea un objeto de autenticación
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null, // No necesitamos credenciales (password) aquí
                            userDetails.getAuthorities() // Roles/Permisos
                    );
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    // 8. Establece la autenticación en el contexto de seguridad de Spring
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
            // 9. Pasa la petición al siguiente filtro en la cadena
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            // Manejo básico de excepciones durante la validación del token
            // En una app real, podrías querer manejar diferentes tipos de excepciones JWT
            // (ExpiredJwtException, MalformedJwtException, etc.) de forma más específica
            // y quizás devolver una respuesta 401 directamente desde aquí.
            logger.warn("Error al procesar el token JWT: {}" + e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // Devuelve 401 si el token es inválido
            response.getWriter().write("Token JWT inválido o expirado");
            // Importante: No continuar con filterChain.doFilter si el token es inválido
            return;
        }
    }

    private String getJwtFromCookies(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        // Busca la cookie con el nombre específico
        return Arrays.stream(cookies)
                .filter(cookie -> "jwt_token".equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }
}
