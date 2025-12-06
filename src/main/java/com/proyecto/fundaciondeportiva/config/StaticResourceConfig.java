package com.proyecto.fundaciondeportiva.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        // ⚠️ AJUSTA ESTA RUTA SEGÚN DONDE GUARDAS LOS ARCHIVOS

        // Para LOCAL (si guardas en una carpeta "uploads" al lado del jar):
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");

        // Si en Azure los guardas en otra ruta, por ejemplo:
        // registry.addResourceHandler("/uploads/**")
        //         .addResourceLocations("file:/home/site/wwwroot/uploads/");
    }
}
