package com.proyecto.fundaciondeportiva.controller;

import com.proyecto.fundaciondeportiva.dto.input.LoginInputDTO;
import com.proyecto.fundaciondeportiva.dto.output.LoginOutputDTO;
import com.proyecto.fundaciondeportiva.model.entity.Usuario;
import com.proyecto.fundaciondeportiva.model.enums.Rol; // ⭐ AGREGAR ESTA LÍNEA
import com.proyecto.fundaciondeportiva.repository.UsuarioRepository;
import com.proyecto.fundaciondeportiva.service.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<LoginOutputDTO> login(
            @Valid @RequestBody LoginInputDTO loginInputDTO,
            HttpServletResponse response
    ) {
        try {
            // 1. Spring Security autentica al usuario
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginInputDTO.getEmail(), loginInputDTO.getPassword())
            );
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            // 2. Buscamos los datos completos del usuario para la respuesta
            Usuario usuario = usuarioRepository.findByEmail(userDetails.getUsername()).orElseThrow();

            // 3. Generamos el token JWT
            String token = jwtService.generateToken(userDetails);

            // 4a. Crear la cookie
            Cookie jwtCookie = new Cookie("jwt_token", token);
            jwtCookie.setHttpOnly(true);
            jwtCookie.setSecure(true);
            jwtCookie.setPath("/");
            jwtCookie.setMaxAge(60 * 60 * 10);

            response.addCookie(jwtCookie);

            // ⭐ CORREGIDO: Obtener DNI solo si es profesor CON MANEJO DE ERRORES
            String dni = null;
            try {
                if (usuario.getRol() == Rol.PROFESOR) {
                    // Buscar perfil de profesor de forma segura
                    if (usuario.getPerfilProfesor() != null) {
                        dni = usuario.getPerfilProfesor().getDni();
                    } else {
                        logger.warn("El profesor {} no tiene perfil asociado", usuario.getEmail());
                    }
                }
            } catch (Exception e) {
                logger.error("Error al obtener DNI del profesor: {}", e.getMessage());
                // Continuar sin DNI en lugar de fallar todo el login
            }

            // 4. Creamos y devolvemos la respuesta
            LoginOutputDTO responseBody = LoginOutputDTO.builder()
                    .token(token)
                    .nombre(usuario.getNombre())
                    .rol(usuario.getRol())
                    .email(usuario.getEmail())
                    .dni(dni)
                    .build();

            return ResponseEntity.ok(responseBody);

        } catch (Exception e) {
            logger.error("Error en login: ", e);
            throw e;
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletResponse response) {
        Cookie jwtCookie = new Cookie("jwt_token", null);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(true);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(0);

        response.addCookie(jwtCookie);

        return ResponseEntity.ok("Cierre de sesión exitoso");
    }
}