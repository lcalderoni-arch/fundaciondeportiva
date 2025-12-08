package com.proyecto.fundaciondeportiva.controller;

import com.proyecto.fundaciondeportiva.dto.response.ETLResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@PostMapping("/etl/usuarios")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public ResponseEntity<ETLResponseDTO> cargarUsuariosPorExcel(
        @RequestParam("file") MultipartFile file) {

    ETLResponseDTO resultado = etlUsuariosService.procesarExcel(file);
    return ResponseEntity.ok(resultado);
}
