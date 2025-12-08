package com.proyecto.fundaciondeportiva.controller;

import com.proyecto.fundaciondeportiva.dto.response.ETLResponseDTO;
import com.proyecto.fundaciondeportiva.service.ETLUsuariosService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/etl")
@CrossOrigin("*")
public class ETLUsuariosController {

    @Autowired
    private ETLUsuariosService etlService;

    @PostMapping("/usuarios")
    public ResponseEntity<ETLResponseDTO> subirExcelUsuarios(
            @RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        ETLResponseDTO response = etlService.procesarExcel(file);
        return ResponseEntity.ok(response);
    }
}
