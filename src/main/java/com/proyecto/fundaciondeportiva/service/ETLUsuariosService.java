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
import org.apache.poi.ss.util.CellUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ETLUsuariosService {

    @Autowired
    private UsuarioRepository usuarioRepo;

    public ETLResponseDTO procesarExcel(MultipartFile file) {

        ETLResponseDTO resultado = new ETLResponseDTO();

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

                    // 2) Leemos con helper que limpia tipos
                    String nombre = getStringCell(row, 0);
                    String email = safeLower(getStringCell(row, 1));
                    String rolStr = safeUpper(getStringCell(row, 2));
                    String dni = getStringCell(row, 3);
                    String telEmergencia = getStringCell(row, 4);
                    String nivelStr = safeUpper(getStringCell(row, 5));
                    String grado = getStringCell(row, 6);
                    String telProfesor = getStringCell(row, 7);
                    String experiencia = getStringCell(row, 8);

                    // 3) Validaciones comunes
                    if (isBlank(nombre) || isBlank(email) || isBlank(rolStr)) {
                        agregarError(resultado, filaActual,
                                "Nombre, email y rol son obligatorios.");
                        continue;
                    }

                    if (!List.of("ALUMNO", "PROFESOR", "ADMINISTRADOR").contains(rolStr)) {
                        agregarError(resultado, filaActual, "Rol inválido: " + rolStr);
                        continue;
                    }

                    if (usuarioRepo.existsByEmail(email)) {
                        agregarError(resultado, filaActual, "Email repetido: " + email);
                        continue;
                    }

                    // 4) Validar DNI según rol
                    if (!rolStr.equals("ADMINISTRADOR")) {
                        if (isBlank(dni)) {
                            agregarError(resultado, filaActual,
                                    "DNI es obligatorio para ALUMNO y PROFESOR.");
                            continue;
                        }

                        if (usuarioRepo.existsByPerfilAlumno_Dni(dni) ||
                                usuarioRepo.existsByPerfilProfesor_Dni(dni)) {
                            agregarError(resultado, filaActual,
                                    "DNI repetido: " + dni);
                            continue;
                        }
                    }

                    // 5) Crear usuario base
                    Usuario u = new Usuario();
                    u.setNombre(nombre.trim());
                    u.setEmail(email.trim());
                    u.setRol(Rol.valueOf(rolStr));
                    u.setPassword("{noop}123456"); // O encriptar con BCrypt
                    u.setHabilitadoMatricula(true);

                    // 6) Segun rol, generar perfil
                    if (rolStr.equals("ALUMNO")) {

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
                        pa.setDni(dni);
                        pa.setNivel(nivelEnum);
                        pa.setGrado(grado.trim());
                        pa.setTelefonoEmergencia(telEmergencia.trim());

                        // Enlazar ambas entidades
                        pa.setUsuario(u);
                        u.setPerfilAlumno(pa);

                    } else if (rolStr.equals("PROFESOR")) {

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
                    } else {
                        // ADMINISTRADOR -> no requiere perfil
                    }

                    // 7) Guardamos todo
                    usuarioRepo.save(u);
                    resultado.setExitosos(resultado.getExitosos() + 1);

                } catch (Exception ex) {
                    agregarError(resultado, filaActual, "Error inesperado: " + ex.getMessage());
                }
            }

        } catch (Exception ex) {
            throw new RuntimeException("Error leyendo Excel: " + ex.getMessage(), ex);
        }

        resultado.setFallidos(resultado.getProcesados() - resultado.getExitosos());
        return resultado;
    }

    // =======================
    //   HELPERS "LIMPIEZA"
    // =======================

    private boolean esFilaVacia(Row row) {
        if (row == null) return true;
        for (int i = 0; i <= 8; i++) { // revisamos solo las primeras 9 columnas
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String v = getStringCell(row, i);
                if (!isBlank(v)) return false;
            }
        }
        return true;
    }

    private String getStringCell(Row row, int index) {
        if (row == null) return null;
        Cell cell = row.getCell(index);
        if (cell == null) return null;

        // Normalizamos tipo
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                // evita notación científica
                BigDecimal bd = BigDecimal.valueOf(cell.getNumericCellValue());
                yield bd.toPlainString();
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> {
                // evaluamos como string si es posible
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
            // por si no se inicializó la lista
            res.setErrores(new java.util.ArrayList<>());
        }
        ErrorFilaDTO error = new ErrorFilaDTO();
        error.setFila(fila);
        error.setMensaje(msg);
        res.getErrores().add(error);
    }
}
