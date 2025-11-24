package com.proyecto.fundaciondeportiva.controller;

import com.proyecto.fundaciondeportiva.dto.input.LoginInputDTO;
import com.proyecto.fundaciondeportiva.dto.output.LoginOutputDTO;
import com.proyecto.fundaciondeportiva.model.entity.Usuario;
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

        // 4. Creamos y devolvemos la respuesta
        LoginOutputDTO responseBody = LoginOutputDTO.builder()
                .token(token)
                .nombre(usuario.getNombre())
                .rol(usuario.getRol())
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