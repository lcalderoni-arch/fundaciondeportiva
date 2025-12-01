package com.proyecto.fundaciondeportiva.model.entity;

import com.proyecto.fundaciondeportiva.model.enums.Rol;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "usuarios", uniqueConstraints = {
        @UniqueConstraint(columnNames = "email")
})
public class Usuario implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100, nullable = false)
    private String nombre;

    @Column(length = 100, nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Rol rol;

    @CreationTimestamp
    @Column(name = "fecha_creacion", updatable = false, nullable = false)
    private LocalDateTime fechaCreacion;

    // --- Relaciones 1:1 con Perfiles ---

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "perfil_alumno_id", referencedColumnName = "id")
    @EqualsAndHashCode.Exclude // ✅ Correcto (Ya lo tienes)
    @ToString.Exclude // Recomendado: Evita ciclos al imprimir logs
    private PerfilAlumno perfilAlumno;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "perfil_profesor_id", referencedColumnName = "id")
    @EqualsAndHashCode.Exclude // ⚠️ AGREGAR ESTO (Para evitar error con profesores)
    @ToString.Exclude
    private PerfilProfesor perfilProfesor;

    // --- Relaciones Listas (Es mejor excluirlas también del HashCode y ToString por rendimiento y seguridad) ---

    @OneToMany(mappedBy = "creadoPor")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Set<Curso> cursosCreados;

    @OneToMany(mappedBy = "profesor")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Set<Seccion> seccionesAsignadas;

    @OneToMany(mappedBy = "alumno")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Set<Matricula> matriculas;

    @OneToMany(mappedBy = "alumno")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Set<Asistencia> asistencias;

    // --- Métodos de Spring Security (UserDetails) ---
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + rol.name()));
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}