package com.proyecto.fundaciondeportiva.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {
        "perfilAlumno",
        "perfilProfesor",
        "cursosCreados",
        "seccionesAsignadas",
        "matriculas",
        "asistencias",
        "password" // opcional, para no loguear el password
})
@Entity
@Table(name = "usuarios", uniqueConstraints = {
        @UniqueConstraint(columnNames = "email")
})
public class Usuario implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(length = 100, nullable = false)
    private String nombre;

    @Column(length = 100, nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    @JsonIgnore // opcional, para que no salga en respuestas JSON
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
    private PerfilAlumno perfilAlumno;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "perfil_profesor_id", referencedColumnName = "id")
    private PerfilProfesor perfilProfesor;

    // --- Relaciones (otros lados) ---
    @OneToMany(mappedBy = "creadoPor")
    @JsonIgnore
    private Set<Curso> cursosCreados;

    @OneToMany(mappedBy = "profesor")
    @JsonIgnore
    private Set<Seccion> seccionesAsignadas;

    @OneToMany(mappedBy = "alumno")
    @JsonIgnore
    private Set<Matricula> matriculas;

    @OneToMany(mappedBy = "alumno")
    @JsonIgnore
    private Set<Asistencia> asistencias;

    // --- Métodos de Spring Security (UserDetails) ---
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + rol.name()));
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
