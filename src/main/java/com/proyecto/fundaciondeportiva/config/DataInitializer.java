package com.proyecto.fundaciondeportiva.config;

import com.proyecto.fundaciondeportiva.model.entity.ConfiguracionMatricula;
import com.proyecto.fundaciondeportiva.model.entity.Usuario;
import com.proyecto.fundaciondeportiva.model.enums.Rol;
import com.proyecto.fundaciondeportiva.repository.ConfiguracionMatriculaRepository;
import com.proyecto.fundaciondeportiva.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ConfiguracionMatriculaRepository configuracionMatriculaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {

        // 1) Admin inicial
        if (usuarioRepository.count() == 0) {
            System.out.println("No se encontraron usuarios, creando usuario administrador inicial...");

            Usuario admin = new Usuario();
            admin.setNombre("Admin Principal");
            admin.setEmail("admin@fundacion.com");
            admin.setPassword(passwordEncoder.encode("adminfundacion_2025"));
            admin.setRol(Rol.ADMINISTRADOR);

            usuarioRepository.save(admin);

            System.out.println("Usuario administrador creado con éxito.");
        }

        // 2) Configuración matrícula inicial
        if (configuracionMatriculaRepository.count() == 0) {
            System.out.println("No se encontró configuración de matrícula, creando configuración inicial...");

            ConfiguracionMatricula config = new ConfiguracionMatricula();
            config.setMatriculaHabilitada(false);
            config.setFechaInicio(null);
            config.setFechaFin(null);

            configuracionMatriculaRepository.save(config);

            System.out.println("Configuración de matrícula creada con éxito.");
        }
    }
}
