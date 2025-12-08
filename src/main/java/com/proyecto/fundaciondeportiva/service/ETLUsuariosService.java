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
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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

                try {
                    // Evitar filas completamente vacías
                    if (row == null || rowIsEmpty(row)) {
                        continue;
                    }

                    String nombre = getStringCell(row, 0);
                    String email = getStringCell(row, 1);
                    String rolStr = getStringCell(row, 2);
                    String dni = getStringCell(row, 3);
                    String telefono = getStringCell(row, 4);
                    String nivelStr = getStringCell(row, 5);
                    String grado = getStringCell(row, 6);
                    String experiencia = getStringCell(row, 7);

                    resultado.setProcesados(resultado.getProcesados() + 1);

                    // Normalizar
                    if (email != null) email = email.trim().toLowerCase();
                    if (rolStr != null) rolStr = rolStr.trim().toUpperCase();
                    if (dni != null) dni = dni.trim();

                    // VALIDACIONES BÁSICAS
                    if (nombre == null || nombre.isBlank()) {
                        agregarError(resultado, filaActual, "Nombre vacío");
                        continue;
                    }

                    if (email == null || email.isBlank()) {
                        agregarError(resultado, filaActual, "Email vacío");
                        continue;
                    }

                    if (rolStr == null || rolStr.isBlank()) {
                        agregarError(resultado, filaActual, "Rol vacío");
                        continue;
                    }

                    if (!List.of("ALUMNO", "PROFESOR", "ADMINISTRADOR").contains(rolStr)) {
                        agregarError(resultado, filaActual, "Rol inválido: " + rolStr);
                        continue;
                    }

                    // Email repetido
                    if (usuarioRepo.existsByEmail(email)) {
                        agregarError(resultado, filaActual, "Email repetido: " + email);
                        continue;
                    }

                    // DNI obligatorio para alumno/profesor
                    if (!"ADMINISTRADOR".equals(rolStr)) {
                        if (dni == null || dni.isBlank()) {
                            agregarError(resultado, filaActual, "DNI obligatorio para " + rolStr);
                            continue;
                        }

                        if (usuarioRepo.existsByDni(dni)) {
                            agregarError(resultado, filaActual, "DNI repetido: " + dni);
                            continue;
                        }
                    }

                    // CREAR USUARIO
                    Usuario u = new Usuario();
                    u.setNombre(nombre.trim());
                    u.setEmail(email);
                    u.setRol(Rol.valueOf(rolStr));
                    u.setPassword("123456"); // TODO: encriptar con PasswordEncoder
                    u.setHabilitadoMatricula(true);

                    // Si es ALUMNO, creamos PerfilAlumno
                    if ("ALUMNO".equals(rolStr)) {
                        PerfilAlumno pa = new PerfilAlumno();
                        pa.setDni(dni);

                        NivelAcademico nivel = parseNivel(nivelStr);
                        if (nivel == null) {
                            agregarError(resultado, filaActual,
                                    "Nivel inválido para alumno: " + nivelStr);
                            continue;
                        }
                        pa.setNivel(nivel);

                        if (grado == null || grado.isBlank()) {
                            agregarError(resultado, filaActual,
                                    "Grado obligatorio para alumno");
                            continue;
                        }
                        pa.setGrado(grado.trim());

                        if (telefono != null && !telefono.isBlank()) {
                            pa.setTelefonoEmergencia(telefono.trim());
                        }

                        pa.setUsuario(u);
                        u.setPerfilAlumno(pa);
                    }

                    // Si es PROFESOR, creamos PerfilProfesor
                    if ("PROFESOR".equals(rolStr)) {
                        PerfilProfesor pp = new PerfilProfesor();
                        pp.setDni(dni);

                        if (telefono != null && !telefono.isBlank()) {
                            pp.setTelefono(telefono.trim());
                        }

                        if (experiencia != null && !experiencia.isBlank()) {
                            pp.setExperiencia(experiencia.trim());
                        }

                        pp.setUsuario(u);
                        u.setPerfilProfesor(pp);
                    }

                    usuarioRepo.save(u);
                    resultado.setExitosos(resultado.getExitosos() + 1);

                } catch (Exception ex) {
                    agregarError(resultado, filaActual,
                            "Error inesperado: " + ex.getMessage());
                }
            }

        } catch (Exception ex) {
            throw new RuntimeException("Error leyendo Excel: " + ex.getMessage(), ex);
        }

        resultado.setFallidos(resultado.getProcesados() - resultado.getExitosos());
        return resultado;
    }

    private void agregarError(ETLResponseDTO res, int fila, String msg) {
        ErrorFilaDTO error = new ErrorFilaDTO();
        error.setFila(fila);
        error.setMensaje(msg);
        res.getErrores().add(error);
    }

    // Helpers para celdas seguras
    private String getStringCell(Row row, int index) {
        Cell cell = row.getCell(index);
        if (cell == null) return null;

        cell.setCellType(CellType.STRING);
        String value = cell.getStringCellValue();
        return value != null ? value.trim() : null;
    }

    private boolean rowIsEmpty(Row row) {
        if (row == null) return true;
        for (int c = 0; c < 8; c++) { // revisamos las primeras 8 columnas
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String s = cell.toString();
                if (s != null && !s.trim().isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    private NivelAcademico parseNivel(String nivelStr) {
        if (nivelStr == null) return null;
        String n = nivelStr.trim().toUpperCase();

        // Ajusta según los valores reales de tu enum NivelAcademico
        // Ejemplo: INICIAL, PRIMARIA, SECUNDARIA
        try {
            return NivelAcademico.valueOf(n);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}