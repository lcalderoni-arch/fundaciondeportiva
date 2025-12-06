// src/main/java/com/proyecto/fundaciondeportiva/service/RecursoService.java
package com.proyecto.fundaciondeportiva.service;

import com.proyecto.fundaciondeportiva.dto.request.RecursoRequest;
import com.proyecto.fundaciondeportiva.dto.response.RecursoDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface RecursoService {

    List<RecursoDTO> listarPorSesion(Long sesionId);

    RecursoDTO crearRecurso(RecursoRequest request, String emailProfesor);

    RecursoDTO crearRecursoArchivo(
            Long sesionId,
            String titulo,
            String descripcion,
            String momentoStr,
            String tipoRecursoStr,
            MultipartFile archivo,
            String emailProfesor
    );

    RecursoDTO actualizarRecurso(Long id, RecursoRequest request, String emailProfesor);

    void eliminarRecurso(Long id, String emailProfesor);
}