package com.proyecto.fundaciondeportiva.service;

import com.proyecto.fundaciondeportiva.dto.response.EntregaTareaDTO;

import java.util.List;

public interface EntregaTareaService {

    EntregaTareaDTO registrarEntrega(Long recursoId,
                                     String titulo,
                                     String descripcion,
                                     String archivoUrl,
                                     String emailAlumno);

    List<EntregaTareaDTO> listarEntregasPorRecurso(Long recursoId, String emailProfesor);
}