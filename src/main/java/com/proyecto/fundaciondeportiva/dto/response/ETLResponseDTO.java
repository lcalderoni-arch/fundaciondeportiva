package com.proyecto.fundaciondeportiva.dto.response;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ETLResponseDTO {

    private int procesados;
    private int exitosos;
    private int fallidos;

    private List<ErrorFilaDTO> errores = new ArrayList<>();
}
