package com.proyecto.fundaciondeportiva.controller;

import com.proyecto.fundaciondeportiva.service.DataCleaningService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/data-quality")
public class DataQualityController {

    private final DataCleaningService cleaningService;

    public DataQualityController(DataCleaningService cleaningService) {
        this.cleaningService = cleaningService;
    }

    @PostMapping("/limpiar-eventos")
    public Map<String, Object> limpiarEventos() {
        int n = cleaningService.limpiarEventosNuevos();
        Map<String, Object> resp = new HashMap<>();
        resp.put("eventosProcesados", n);
        return resp;
    }
}
