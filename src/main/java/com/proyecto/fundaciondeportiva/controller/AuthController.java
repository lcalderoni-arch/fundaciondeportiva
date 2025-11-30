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

@RestController
@RequestMapping("/api/auth")
public class AuthController {

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

        String dni = null;
        String nivelAlumno = null;
        String gradoAlumno = null;

        // Si es PROFESOR, obtenemos DNI del perfil profesor (como ya lo tenías)
        if (usuario.getRol() == Rol.PROFESOR && usuario.getPerfilProfesor() != null) {
            dni = usuario.getPerfilProfesor().getDni();
        }

        // ⭐ SI ES ALUMNO, sacamos NIVEL y GRADO del PerfilAlumno
        if (usuario.getRol() == Rol.ALUMNO && usuario.getPerfilAlumno() != null) {
            // si quieres también puedes mandar el dni del alumno:
            // dni = usuario.getPerfilAlumno().getDni();

            // Si 'nivel' es un enum, .name() te devolverá "INICIAL", "PRIMARIA", etc.
            nivelAlumno = usuario.getPerfilAlumno().getNivel() != null
                    ? usuario.getPerfilAlumno().getNivel().name()
                    : null;

            gradoAlumno = usuario.getPerfilAlumno().getGrado();
        }

        // 4. Creamos y devolvemos la respuesta
        LoginOutputDTO responseBody = LoginOutputDTO.builder()
                .token(token)
                .nombre(usuario.getNombre())
                .rol(usuario.getRol())
                .email(usuario.getEmail())
                .dni(dni)
                .nivelAlumno(nivelAlumno)   // ⭐ NUEVO
                .gradoAlumno(gradoAlumno)   // ⭐ NUEVO
                .build();

        return ResponseEntity.ok(responseBody);
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