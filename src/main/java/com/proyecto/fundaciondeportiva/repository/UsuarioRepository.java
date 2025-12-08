package com.proyecto.fundaciondeportiva.repository;

import com.proyecto.fundaciondeportiva.model.entity.Usuario;
import com.proyecto.fundaciondeportiva.model.enums.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Usuario.
 * Reemplaza al 'UsuarioRepository' anterior.
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    // Buscar DNI en perfil alumno
    boolean existsByPerfilAlumno_Dni(String dni);

    // Buscar DNI en perfil profesor
    boolean existsByPerfilProfesor_Dni(String dni);

    @Query("""
           SELECT COUNT(u) > 0
           FROM Usuario u
           LEFT JOIN u.perfilAlumno pa
           LEFT JOIN u.perfilProfesor pp
           WHERE (pa.dni IS NOT NULL AND pa.dni = :dni)
              OR (pp.dni IS NOT NULL AND pp.dni = :dni)
           """)
    boolean existsByDni(@Param("dni") String dni);

    // 👉 NUEVO: para listar por rol (ALUMNO, PROFESOR, ADMINISTRADOR)
    List<Usuario> findByRol(Rol rol);

    @Query("SELECT u FROM Usuario u WHERE u.rol = com.proyecto.fundaciondeportiva.model.enums.Rol.ALUMNO")
    List<Usuario> findAllAlumnos();
}
