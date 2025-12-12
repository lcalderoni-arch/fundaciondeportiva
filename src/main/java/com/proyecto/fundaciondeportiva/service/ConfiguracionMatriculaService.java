package com.proyecto.fundaciondeportiva.service;

import com.proyecto.fundaciondeportiva.dto.response.ConfiguracionMatriculaResponse;

import java.time.LocalDate;

public interface ConfiguracionMatriculaService {

    ConfiguracionMatriculaResponse obtenerConfiguracionMatricula();

    ConfiguracionMatriculaResponse actualizarFechasMatricula(LocalDate fechaInicio, LocalDate fechaFin);

    ConfiguracionMatriculaResponse actualizarPermisoGlobalMatricula(boolean habilitada);

    ConfiguracionMatriculaResponse actualizarCicloActual(String cicloActual);

}
