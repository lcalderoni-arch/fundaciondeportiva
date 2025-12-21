package com.proyecto.fundaciondeportiva.controller;

import com.proyecto.fundaciondeportiva.dto.input.LoginInputDTO;
import com.proyecto.fundaciondeportiva.dto.output.LoginOutputDTO;
import com.proyecto.fundaciondeportiva.model.entity.Usuario;
import com.proyecto.fundaciondeportiva.model.enums.Rol;
import com.proyecto.fundaciondeportiva.repository.UsuarioRepository;
import com.proyecto.fundaciondeportiva.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final String REFRESH_COOKIE_NAME = "refresh_token";

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserDetailsService userDetailsService;

    @PostMapping("/login")
    public ResponseEntity<LoginOutputDTO> login(
            @Valid @RequestBody LoginInputDTO loginInputDTO,
            HttpServletRequest request
    ) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginInputDTO.getEmail(), loginInputDTO.getPassword())
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Usuario usuario = usuarioRepository.findByEmail(userDetails.getUsername()).orElseThrow();

        // ✅ Access (corto) -> lo envías en el body
        String accessToken = jwtService.generateAccessToken(userDetails);

        // ✅ Refresh (largo) -> cookie HttpOnly
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        String dni = null;
        String nivelAlumno = null;
        String gradoAlumno = null;

        if (usuario.getRol() == Rol.PROFESOR && usuario.getPerfilProfesor() != null) {
            dni = usuario.getPerfilProfesor().getDni();
        }

        if (usuario.getRol() == Rol.ALUMNO && usuario.getPerfilAlumno() != null) {
            nivelAlumno = usuario.getPerfilAlumno().getNivel() != null
                    ? usuario.getPerfilAlumno().getNivel().name()
                    : null;
            gradoAlumno = usuario.getPerfilAlumno().getGrado();
        }

        LoginOutputDTO responseBody = LoginOutputDTO.builder()
                .token(accessToken)
                .nombre(usuario.getNombre())
                .rol(usuario.getRol())
                .email(usuario.getEmail())
                .dni(dni)
                .nivelAlumno(nivelAlumno)
                .gradoAlumno(gradoAlumno)
                .build();

        boolean isHttps = request.isSecure();

        ResponseCookie refreshCookie = ResponseCookie.from(REFRESH_COOKIE_NAME, refreshToken)
                .httpOnly(true)
                .secure(isHttps)                 // prod https: true / local http: false
                .path("/api/auth")               // solo se manda a /api/auth/*
                .sameSite(isHttps ? "None" : "Lax")
                .maxAge(60L * 60 * 24 * 7)       // 7 días
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(responseBody);
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginOutputDTO> refresh(HttpServletRequest request) {
        String refreshToken = readCookie(request, REFRESH_COOKIE_NAME);
        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.status(401).build();
        }

        final String email;
        try {
            email = jwtService.extractUsername(refreshToken);
        } catch (Exception e) {
            return ResponseEntity.status(401).build();
        }

        // ✅ carga user “oficial” del security
        UserDetails userDetails;
        try {
            userDetails = userDetailsService.loadUserByUsername(email);
        } catch (Exception e) {
            return ResponseEntity.status(401).build();
        }

        if (!jwtService.validateRefreshToken(refreshToken, userDetails)) {
            return ResponseEntity.status(401).build();
        }

        // ✅ nuevo access token
        String newAccessToken = jwtService.generateAccessToken(userDetails);

        Usuario usuario = usuarioRepository.findByEmail(email).orElseThrow();

        LoginOutputDTO responseBody = LoginOutputDTO.builder()
                .token(newAccessToken)
                .nombre(usuario.getNombre())
                .rol(usuario.getRol())
                .email(usuario.getEmail())
                .build();

        return ResponseEntity.ok(responseBody);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        boolean isHttps = request.isSecure();

        ResponseCookie deleteCookie = ResponseCookie.from(REFRESH_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(isHttps)
                .path("/api/auth")
                .sameSite(isHttps ? "None" : "Lax")
                .maxAge(0)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
                .body("Cierre de sesión exitoso");
    }

    private String readCookie(HttpServletRequest request, String cookieName) {
        if (request.getCookies() == null) return null;
        return Arrays.stream(request.getCookies())
                .filter(c -> cookieName.equals(c.getName()))
                .map(c -> c.getValue())
                .findFirst()
                .orElse(null);
    }
}
