package com.proyecto.fundaciondeportiva.service.impl;

import com.proyecto.fundaciondeportiva.dto.request.SeccionRequestDTO;
import com.proyecto.fundaciondeportiva.dto.response.SeccionResponseDTO;
import com.proyecto.fundaciondeportiva.exception.RecursoNoEncontradoException;
import com.proyecto.fundaciondeportiva.exception.ValidacionException;
import com.proyecto.fundaciondeportiva.model.entity.Curso;
import com.proyecto.fundaciondeportiva.model.entity.PerfilProfesor;
import com.proyecto.fundaciondeportiva.model.entity.Seccion;
import com.proyecto.fundaciondeportiva.model.entity.Sesion;
import com.proyecto.fundaciondeportiva.model.entity.Usuario;
import com.proyecto.fundaciondeportiva.model.enums.NivelAcademico;
import com.proyecto.fundaciondeportiva.model.enums.Rol;
import com.proyecto.fundaciondeportiva.model.enums.Turno;
import com.proyecto.fundaciondeportiva.repository.CursoRepository;
import com.proyecto.fundaciondeportiva.repository.PerfilProfesorRepository;
import com.proyecto.fundaciondeportiva.repository.SeccionRepository;
import com.proyecto.fundaciondeportiva.repository.SesionRepository;
import com.proyecto.fundaciondeportiva.repository.UsuarioRepository;
import com.proyecto.fundaciondeportiva.service.ServicioSeccion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ServicioSeccionImpl implements ServicioSeccion {

    private static final Logger logger = LoggerFactory.getLogger(ServicioSeccionImpl.class);

    @Autowired
    private SeccionRepository seccionRepository;

    @Autowired
    private SesionRepository sesionRepository;

    @Autowired
    private CursoRepository cursoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PerfilProfesorRepository perfilProfesorRepository;

    // ===========================
    //        LISTADOS
    // ===========================

    @Override
    @Transactional(readOnly = true)
    public List<SeccionResponseDTO> listarTodasLasSecciones() {
        logger.info("Listando todas las secciones");
        return seccionRepository.findAll()
                .stream()
                .map(SeccionResponseDTO::deEntidad)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SeccionResponseDTO> listarSeccionesActivas() {
        logger.info("Listando secciones activas");
        return seccionRepository.findByActivaTrue()
                .stream()
                .map(SeccionResponseDTO::deEntidad)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SeccionResponseDTO obtenerSeccionPorId(Long id) {
        logger.info("Obteniendo sección con id: {}", id);
        Seccion seccion = seccionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Sección no encontrada con id: " + id));
        return SeccionResponseDTO.deEntidad(seccion);
    }

    // ===========================
    //   CREAR / ACTUALIZAR / BORRAR
    // ===========================

    @Override
    @Transactional
    public SeccionResponseDTO crearSeccion(SeccionRequestDTO request) {
        logger.info("Creando nueva sección: {}", request.getNombre());

        try {
            // Validar fechas
            validarFechas(request.getFechaInicio(), request.getFechaFin());

            // Buscar curso
            Curso curso = cursoRepository.findById(request.getCursoId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Curso no encontrado con id: " + request.getCursoId()));

            // Validar que el nivel de la sección coincida con el nivel del curso
            if (!request.getNivelSeccion().equals(curso.getNivelDestino())) {
                throw new ValidacionException("El nivel de la sección debe coincidir con el nivel del curso");
            }

            // Buscar profesor por DNI
            Usuario profesor = buscarProfesorPorDni(request.getProfesorDni());

            // Generar código único
            String codigoGenerado = generarCodigoUnico();
            logger.info("Código de sección generado: {}", codigoGenerado);

            // Crear sección
            Seccion nuevaSeccion = Seccion.builder()
                    .codigo(codigoGenerado)
                    .nombre(request.getNombre())
                    .nivelSeccion(request.getNivelSeccion())
                    .gradoSeccion(request.getGradoSeccion())
                    .turno(request.getTurno())
                    .aula(request.getAula())
                    .capacidad(request.getCapacidad())
                    .fechaInicio(request.getFechaInicio())
                    .fechaFin(request.getFechaFin())
                    .numeroSemanas(request.getNumeroSemanas())
                    .activa(true)
                    .curso(curso)
                    .profesor(profesor)
                    .build();

            // ⭐ GENERAR SEMANAS (SemanaSemana) EN LA ENTIDAD SECCIÓN
            logger.info("Generando {} semanas académicas...", request.getNumeroSemanas());
            nuevaSeccion.generarSemanas();
            logger.info("Semanas generadas en la entidad Seccion (no sesiones aún)");

            // Guardar sección con sus semanas
            Seccion seccionGuardada = seccionRepository.save(nuevaSeccion);
            logger.info("Sección creada exitosamente con ID: {}", seccionGuardada.getId());
            logger.info("Total de semanas generadas: {}",
                    seccionGuardada.getSemanas() != null ? seccionGuardada.getSemanas().size() : 0);

            // ⭐ NUEVO: GENERAR SESIONES (tabla sesiones)
            generarSesionesParaSeccion(seccionGuardada);

            return SeccionResponseDTO.deEntidad(seccionGuardada);

        } catch (RecursoNoEncontradoException | ValidacionException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error al crear sección", e);
            throw new RuntimeException("Error al crear la sección: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public SeccionResponseDTO actualizarSeccion(Long id, SeccionRequestDTO request) {
        logger.info("Actualizando sección con id: {}", id);

        try {
            Seccion seccionExistente = seccionRepository.findById(id)
                    .orElseThrow(() -> new RecursoNoEncontradoException("Sección no encontrada con id: " + id));

            // Validar fechas
            validarFechas(request.getFechaInicio(), request.getFechaFin());

            // Buscar curso
            Curso curso = cursoRepository.findById(request.getCursoId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Curso no encontrado con id: " + request.getCursoId()));

            // Validar que el nivel de la sección coincida con el nivel del curso
            if (!request.getNivelSeccion().equals(curso.getNivelDestino())) {
                throw new ValidacionException("El nivel de la sección debe coincidir con el nivel del curso");
            }

            // Buscar profesor por DNI
            Usuario profesor = buscarProfesorPorDni(request.getProfesorDni());

            // Actualizar campos básicos
            seccionExistente.setNombre(request.getNombre());
            seccionExistente.setNivelSeccion(request.getNivelSeccion());
            seccionExistente.setGradoSeccion(request.getGradoSeccion());
            seccionExistente.setTurno(request.getTurno());
            seccionExistente.setAula(request.getAula());
            seccionExistente.setCapacidad(request.getCapacidad());
            seccionExistente.setFechaInicio(request.getFechaInicio());
            seccionExistente.setFechaFin(request.getFechaFin());
            seccionExistente.setCurso(curso);
            seccionExistente.setProfesor(profesor);

            boolean cambioNumeroSemanas =
                    !seccionExistente.getNumeroSemanas().equals(request.getNumeroSemanas());

            if (cambioNumeroSemanas) {
                logger.info("Número de semanas cambió de {} a {}. Regenerando semanas y sesiones...",
                        seccionExistente.getNumeroSemanas(), request.getNumeroSemanas());

                seccionExistente.setNumeroSemanas(request.getNumeroSemanas());

                // Regenerar semanas (SemanaSemana)
                seccionExistente.generarSemanas();

                // Guardar cambios de la sección y sus semanas
                Seccion seccionConSemanas = seccionRepository.save(seccionExistente);

                // 🔥 BORRAR SESIONES ANTIGUAS Y RECREARLAS
                List<Sesion> sesionesAntiguas =
                        sesionRepository.findBySeccionIdOrderByFechaAsc(seccionConSemanas.getId());
                if (!sesionesAntiguas.isEmpty()) {
                    sesionRepository.deleteAll(sesionesAntiguas);
                }

                generarSesionesParaSeccion(seccionConSemanas);

                logger.info("Semanas y sesiones regeneradas correctamente para la sección {}", seccionConSemanas.getId());
                return SeccionResponseDTO.deEntidad(seccionConSemanas);
            } else {
                // Si no cambió el número de semanas, solo guardamos lo demás
                Seccion seccionActualizada = seccionRepository.save(seccionExistente);
                logger.info("Sección actualizada exitosamente: {}", seccionActualizada.getId());
                return SeccionResponseDTO.deEntidad(seccionActualizada);
            }

        } catch (RecursoNoEncontradoException | ValidacionException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error al actualizar sección", e);
            throw new RuntimeException("Error al actualizar la sección: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void eliminarSeccion(Long id) {
        logger.info("Eliminando sección con id: {}", id);

        Seccion seccion = seccionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Sección no encontrada con id: " + id));

        // Validar que no tenga estudiantes matriculados
        if (seccion.getNumeroEstudiantesMatriculados() > 0) {
            throw new ValidacionException("No se puede eliminar una sección con estudiantes matriculados. " +
                    "Actualmente tiene " + seccion.getNumeroEstudiantesMatriculados() + " estudiante(s).");
        }

        try {
            seccionRepository.deleteById(id);
            logger.info("Sección eliminada exitosamente: {}", id);
        } catch (Exception e) {
            logger.error("Error al eliminar sección", e);
            throw new RuntimeException("Error al eliminar la sección: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void desactivarSeccion(Long id) {
        logger.info("Desactivando sección con id: {}", id);

        Seccion seccion = seccionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Sección no encontrada con id: " + id));

        if (!seccion.getActiva()) {
            throw new ValidacionException("La sección ya está desactivada");
        }

        seccion.setActiva(false);
        seccionRepository.save(seccion);
        logger.info("Sección desactivada exitosamente: {}", id);
    }

    @Override
    @Transactional
    public void activarSeccion(Long id) {
        logger.info("Activando sección con id: {}", id);

        Seccion seccion = seccionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Sección no encontrada con id: " + id));

        if (seccion.getActiva()) {
            throw new ValidacionException("La sección ya está activa");
        }

        seccion.setActiva(true);
        seccionRepository.save(seccion);
        logger.info("Sección activada exitosamente: {}", id);
    }

    // ===========================
    //  LISTADOS ESPECÍFICOS
    // ===========================

    @Override
    @Transactional(readOnly = true)
    public List<SeccionResponseDTO> listarSeccionesPorCurso(Long cursoId) {
        logger.info("Listando secciones del curso: {}", cursoId);

        // Validar que el curso existe
        if (!cursoRepository.existsById(cursoId)) {
            throw new RecursoNoEncontradoException("Curso no encontrado con id: " + cursoId);
        }

        return seccionRepository.findByCursoId(cursoId)
                .stream()
                .map(SeccionResponseDTO::deEntidad)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SeccionResponseDTO> listarSeccionesPorProfesor(Long profesorId) {
        logger.info("Listando secciones del profesor: {}", profesorId);

        // Validar que el profesor existe
        Usuario profesor = usuarioRepository.findById(profesorId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Profesor no encontrado con id: " + profesorId));

        if (profesor.getRol() != Rol.PROFESOR) {
            throw new ValidacionException("El usuario no es un profesor");
        }

        return seccionRepository.findByProfesorId(profesorId)
                .stream()
                .map(SeccionResponseDTO::deEntidad)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SeccionResponseDTO> listarSeccionesPorTurno(Turno turno) {
        logger.info("Listando secciones del turno: {}", turno);
        return seccionRepository.findByTurno(turno)
                .stream()
                .map(SeccionResponseDTO::deEntidad)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SeccionResponseDTO> listarSeccionesConCupo() {
        logger.info("Listando secciones con cupo disponible");
        return seccionRepository.findSeccionesConCupoDisponible()
                .stream()
                .map(SeccionResponseDTO::deEntidad)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SeccionResponseDTO> listarSeccionesPorNivel(NivelAcademico nivel) {
        logger.info("Listando secciones del nivel: {}", nivel);
        return seccionRepository.findByNivelAndActiva(nivel)
                .stream()
                .map(SeccionResponseDTO::deEntidad)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SeccionResponseDTO> listarSeccionesPorDniProfesor(String dni) {
        logger.info("Listando secciones del profesor con DNI: {}", dni);

        // Buscar el perfil de profesor por DNI
        PerfilProfesor perfilProfesor = perfilProfesorRepository.findByDni(dni)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró un profesor con DNI: " + dni));

        // Obtener el usuario asociado al perfil
        Usuario profesor = perfilProfesor.getUsuario();

        if (profesor == null) {
            throw new RecursoNoEncontradoException("El perfil de profesor no tiene un usuario asociado");
        }

        // Verificar que sea profesor
        if (profesor.getRol() != Rol.PROFESOR) {
            throw new ValidacionException("El usuario no tiene rol de profesor");
        }

        logger.info("Profesor encontrado: {} (ID: {})", profesor.getNombre(), profesor.getId());

        // Buscar secciones por ID del profesor
        return seccionRepository.findByProfesorId(profesor.getId())
                .stream()
                .map(SeccionResponseDTO::deEntidad)
                .collect(Collectors.toList());
    }

    // ===========================
    //      MÉTODOS PRIVADOS
    // ===========================

    /**
     * Valida que las fechas sean consistentes
     */
    private void validarFechas(LocalDate fechaInicio, LocalDate fechaFin) {
        if (fechaInicio == null || fechaFin == null) {
            throw new ValidacionException("Las fechas de inicio y fin son obligatorias");
        }

        if (fechaInicio.isAfter(fechaFin)) {
            throw new ValidacionException("La fecha de inicio no puede ser posterior a la fecha de fin");
        }

        if (fechaFin.isBefore(LocalDate.now())) {
            throw new ValidacionException("La fecha de fin no puede ser anterior a la fecha actual");
        }

        // Validar que el periodo no sea demasiado corto (al menos 1 mes)
        if (fechaInicio.plusMonths(1).isAfter(fechaFin)) {
            throw new ValidacionException("El periodo de la sección debe ser de al menos 1 mes");
        }

        // Validar que el periodo no sea demasiado largo (máximo 1 año)
        if (fechaInicio.plusYears(1).isBefore(fechaFin)) {
            throw new ValidacionException("El periodo de la sección no puede exceder 1 año");
        }
    }

    /**
     * Busca un profesor por su DNI
     */
    private Usuario buscarProfesorPorDni(String dni) {
        logger.debug("Buscando profesor con DNI: {}", dni);

        if (dni == null || dni.trim().isEmpty()) {
            throw new ValidacionException("El DNI del profesor es obligatorio");
        }

        PerfilProfesor perfilProfesor = perfilProfesorRepository.findByDni(dni.trim())
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró un profesor con DNI: " + dni));

        Usuario profesor = perfilProfesor.getUsuario();

        if (profesor == null) {
            throw new RecursoNoEncontradoException("El perfil de profesor no tiene un usuario asociado");
        }

        if (profesor.getRol() != Rol.PROFESOR) {
            throw new ValidacionException("El usuario con DNI " + dni + " no tiene rol de profesor");
        }

        logger.info("Profesor encontrado: {} (ID: {})", profesor.getNombre(), profesor.getId());
        return profesor;
    }

    /**
     * Genera un código único para la sección
     * Formato: SEC-001, SEC-002, etc.
     */
    private String generarCodigoUnico() {
        List<Seccion> todasLasSecciones = seccionRepository.findAll();

        int maxNumero = 0;
        for (Seccion seccion : todasLasSecciones) {
            String codigo = seccion.getCodigo();
            if (codigo != null && codigo.startsWith("SEC-")) {
                try {
                    String numeroStr = codigo.substring(4);
                    int numero = Integer.parseInt(numeroStr);
                    if (numero > maxNumero) {
                        maxNumero = numero;
                    }
                } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
                    logger.warn("Código de sección no numérico o inválido encontrado: {}", codigo);
                }
            }
        }

        int siguienteNumero = maxNumero + 1;
        String codigoGenerado = String.format("SEC-%03d", siguienteNumero);

        // Verificar que no exista (por seguridad)
        int intentos = 0;
        while (seccionRepository.existsByCodigo(codigoGenerado) && intentos < 100) {
            siguienteNumero++;
            codigoGenerado = String.format("SEC-%03d", siguienteNumero);
            intentos++;
        }

        if (intentos >= 100) {
            logger.error("No se pudo generar un código único después de 100 intentos");
            throw new RuntimeException("No se pudo generar un código único para la sección");
        }

        return codigoGenerado;
    }

    /**
     * ⭐ NUEVO: genera una sesión por semana para la sección,
     * usando la fechaInicio + i semanas.
     */
    private void generarSesionesParaSeccion(Seccion seccion) {
        Integer numeroSemanas = seccion.getNumeroSemanas();
        LocalDate fechaInicio = seccion.getFechaInicio();

        if (numeroSemanas == null || numeroSemanas <= 0) {
            logger.warn("No se generaron sesiones: numeroSemanas inválido ({}) para seccion {}",
                    numeroSemanas, seccion.getId());
            return;
        }

        if (fechaInicio == null) {
            logger.warn("No se generaron sesiones: fechaInicio es null para seccion {}", seccion.getId());
            return;
        }

        logger.info("Generando {} sesiones (una por semana) para la sección {}", numeroSemanas, seccion.getId());

        for (int i = 0; i < numeroSemanas; i++) {
            LocalDate fechaSesion = fechaInicio.plusWeeks(i);

            Sesion sesion = Sesion.builder()
                    .tema("Semana " + (i + 1))
                    .fecha(fechaSesion)
                    .horaInicio(null)
                    .horaFin(null)
                    .seccion(seccion)
                    .build();

            sesionRepository.save(sesion);
        }

        logger.info("Sesiones generadas correctamente para la sección {}", seccion.getId());
    }
}
