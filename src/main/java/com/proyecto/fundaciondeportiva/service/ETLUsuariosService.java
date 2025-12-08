package com.proyecto.fundaciondeportiva.service;

import com.proyecto.fundaciondeportiva.dto.response.ETLResponseDTO;
import com.proyecto.fundaciondeportiva.dto.response.ErrorFilaDTO;
import com.proyecto.fundaciondeportiva.model.entity.PerfilAlumno;
import com.proyecto.fundaciondeportiva.model.entity.PerfilProfesor;
import com.proyecto.fundaciondeportiva.model.entity.Usuario;
import com.proyecto.fundaciondeportiva.model.enums.NivelAcademico;
import com.proyecto.fundaciondeportiva.model.enums.Rol;
import com.proyecto.fundaciondeportiva.repository.UsuarioRepository;
import org.apache.poi.ss.usermodel.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ETLUsuariosService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public ETLUsuariosService(UsuarioRepository usuarioRepository,
                              PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public ETLResponseDTO procesarExcel(MultipartFile file) {

        ETLResponseDTO resultado = new ETLResponseDTO();

        // Para detectar duplicados dentro del mismo Excel
        Set<String> emailsExcel = new HashSet<>();
        Set<String> dnisExcel = new HashSet<>();

        int duplicadosEnExcel = 0;

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {

            Sheet sheet = workbook.getSheetAt(0);

            int filaActual = 0;

            for (Row row : sheet) {
                filaActual++;

                // Saltar encabezado
                if (filaActual == 1) continue;

                // 1) Fila completamente vacía -> la saltamos sin contarla
                if (esFilaVacia(row)) {
                    continue;
                }

                try {
                    resultado.setProcesados(resultado.getProcesados() + 1);

                    // 2) Leemos columnas (según el orden definido)
                    String nombre = getStringCell(row, 0);
                    String email = safeLower(getStringCell(row, 1));
                    String rolStr = safeUpper(getStringCell(row, 2));
                    String dni = getStringCell(row, 3);
                    String telEmergencia = getStringCell(row, 4);
                    String nivelStr = safeUpper(getStringCell(row, 5));
                    String grado = getStringCell(row, 6);
                    String telProfesor = getStringCell(row, 7);
                    String experiencia = getStringCell(row, 8);
                    String passwordPlano = getStringCell(row, 9);

                    // 3) Validaciones básicas
                    if (isBlank(nombre) || isBlank(email) || isBlank(rolStr)) {
                        agregarError(resultado, filaActual,
                                "Nombre, email y rol son obligatorios.");
                        continue;
                    }

                    Rol rolEnum;
                    try {
                        rolEnum = Rol.valueOf(rolStr);
                    } catch (IllegalArgumentException ex) {
                        agregarError(resultado, filaActual,
                                "Rol inválido: " + rolStr +
                                        ". Usa ADMINISTRADOR, ALUMNO o PROFESOR.");
                        continue;
                    }

                    // Password obligatorio en el Excel
                    if (isBlank(passwordPlano)) {
                        agregarError(resultado, filaActual,
                                "La contraseña es obligatoria en la columna 10.");
                        continue;
                    }
                    if (passwordPlano.length() < 6) {
                        agregarError(resultado, filaActual,
                                "La contraseña debe tener al menos 6 caracteres.");
                        continue;
                    }

                    // 4) Validar duplicados dentro del Excel
                    if (!emailsExcel.add(email)) {
                        agregarError(resultado, filaActual,
                                "Email repetido dentro del Excel: " + email);
                        duplicadosEnExcel++;
                        continue;
                    }

                    if (rolEnum != Rol.ADMINISTRADOR && !isBlank(dni)) {
                        if (!dnisExcel.add(dni)) {
                            agregarError(resultado, filaActual,
                                    "DNI repetido dentro del Excel: " + dni);
                            duplicadosEnExcel++;
                            continue;
                        }
                    }

                    // 5) Validar contra BD: email existente
                    if (usuarioRepository.existsByEmail(email)) {
                        agregarError(resultado, filaActual,
                                "Email ya existente en el sistema: " + email);
                        continue;
                    }

                    // 6) Validar DNI contra BD (para alumnos y profesores)
                    if (rolEnum != Rol.ADMINISTRADOR) {
                        if (isBlank(dni)) {
                            agregarError(resultado, filaActual,
                                    "DNI es obligatorio para ALUMNO y PROFESOR.");
                            continue;
                        }

                        if (usuarioRepository.existsByPerfilAlumno_Dni(dni)
                                || usuarioRepository.existsByPerfilProfesor_Dni(dni)) {
                            agregarError(resultado, filaActual,
                                    "DNI ya existente en el sistema: " + dni);
                            continue;
                        }
                    }

                    // 7) Crear usuario base
                    Usuario u = new Usuario();
                    u.setNombre(nombre.trim());
                    u.setEmail(email.trim());
                    u.setRol(rolEnum);
                    u.setPassword(passwordEncoder.encode(passwordPlano)); // 🔐 siempre encriptada
                    u.setHabilitadoMatricula(true);

                    // 8) Según rol, crear perfil
                    if (rolEnum == Rol.ALUMNO) {

                        // Validaciones específicas
                        if (isBlank(nivelStr) || isBlank(grado) || isBlank(telEmergencia)) {
                            agregarError(resultado, filaActual,
                                    "Para ALUMNO: nivel, grado y teléfono de emergencia son obligatorios.");
                            continue;
                        }

                        NivelAcademico nivelEnum;
                        try {
                            nivelEnum = NivelAcademico.valueOf(nivelStr);
                        } catch (IllegalArgumentException ex) {
                            agregarError(resultado, filaActual,
                                    "Nivel inválido. Usa INICIAL, PRIMARIA o SECUNDARIA.");
                            continue;
                        }

                        PerfilAlumno pa = new PerfilAlumno();
                        pa.setDni(dni.trim());
                        pa.setNivel(nivelEnum);
                        pa.setGrado(grado.trim());
                        pa.setTelefonoEmergencia(telEmergencia.trim());

                        pa.setUsuario(u);
                        u.setPerfilAlumno(pa);

                    } else if (rolEnum == Rol.PROFESOR) {

                        if (isBlank(dni)) {
                            agregarError(resultado, filaActual,
                                    "DNI es obligatorio para PROFESOR.");
                            continue;
                        }

                        PerfilProfesor pp = new PerfilProfesor();
                        pp.setDni(dni.trim());

                        if (!isBlank(telProfesor)) {
                            pp.setTelefono(telProfesor.trim());
                        }
                        if (!isBlank(experiencia)) {
                            pp.setExperiencia(experiencia.trim());
                        }

                        pp.setUsuario(u);
                        u.setPerfilProfesor(pp);
                    }
                    // ADMINISTRADOR -> no requiere perfil extra

                    // 9) Guardar en BD
                    usuarioRepository.save(u);
                    resultado.setExitosos(resultado.getExitosos() + 1);

                } catch (Exception ex) {
                    agregarError(resultado, filaActual,
                            "Error inesperado en la fila: " + ex.getMessage());
                }
            }

        } catch (Exception ex) {
            throw new RuntimeException("Error leyendo Excel: " + ex.getMessage(), ex);
        }

        if (duplicadosEnExcel > 0) {
            agregarError(
                    resultado,
                    0,
                    "⚠ Se detectaron " + duplicadosEnExcel +
                            " registro(s) duplicado(s) dentro del archivo Excel (DNI o email)."
            );
        }

        resultado.setFallidos(resultado.getProcesados() - resultado.getExitosos());
        return resultado;
    }

    // =======================
    //   HELPERS "LIMPIEZA"
    // =======================

    private boolean esFilaVacia(Row row) {
        if (row == null) return true;
        // Revisamos columnas 0..9 (10 columnas)
        for (int i = 0; i <= 9; i++) {
            String v = getStringCell(row, i);
            if (!isBlank(v)) return false;
        }
        return true;
    }

    private String getStringCell(Row row, int index) {
        if (row == null) return null;
        Cell cell = row.getCell(index);
        if (cell == null) return null;

        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                BigDecimal bd = BigDecimal.valueOf(cell.getNumericCellValue());
                yield bd.toPlainString();
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> {
                try {
                    yield cell.getStringCellValue().trim();
                } catch (Exception e) {
                    BigDecimal bd = BigDecimal.valueOf(cell.getNumericCellValue());
                    yield bd.toPlainString();
                }
            }
            default -> null;
        };
    }

    private String safeLower(String s) {
        return s == null ? null : s.toLowerCase().trim();
    }

    private String safeUpper(String s) {
        return s == null ? null : s.toUpperCase().trim();
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private void agregarError(ETLResponseDTO res, int fila, String msg) {
        if (res.getErrores() == null) {
            res.setErrores(new java.util.ArrayList<>());
        }
        ErrorFilaDTO error = new ErrorFilaDTO();
        error.setFila(fila);
        error.setMensaje(msg);
        res.getErrores().add(error);
    }
}
