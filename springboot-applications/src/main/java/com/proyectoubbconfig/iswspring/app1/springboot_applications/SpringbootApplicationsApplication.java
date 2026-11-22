package com.proyectoubbconfig.iswspring.app1.springboot_applications;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.CentroPractica;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Colaborador;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Coordinador;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Documento;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.EnlaceEvaluacion;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.EstadoPractica;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Estudiante;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Informe;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.InteraccionIA;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Notificacion;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Pauta;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Planificacion;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Practica;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Profesor;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Rol;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.TipoEvaluador;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Usuario;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.CentroPracticaRepository;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.ColaboradorRepository;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.DocumentoRepository;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.EnlaceEvaluacionRepository;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.EstudianteRepository;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.InteraccionIARepository;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.NotificacionRepository;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.PautaRepository;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.PlanificacionRepository;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.PracticaRepository;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.ProfesorRepository;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.UsuarioRepository;

@SpringBootApplication
public class SpringbootApplicationsApplication {

    private static final Logger log = LoggerFactory.getLogger(SpringbootApplicationsApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(SpringbootApplicationsApplication.class, args);
    }

    /**
     * Los 3 usuarios base se crean igual que siempre. Después, si demo.datos=true (por defecto),
     * se cargan los datos de demostración. En producción: demo.datos=false.
     */
    @Bean
    public CommandLineRunner crearUsuariosPrueba(UsuarioRepository usuarioRepository,
                                                 PasswordEncoder passwordEncoder,
                                                 DatosDemo datosDemo,
                                                 @Value("${demo.datos:true}") boolean cargarDemo) {
        return args -> {

            Optional<Usuario> coordOpt = usuarioRepository.findByCorreo("coordinador@ubb.cl");
            if (coordOpt.isEmpty()) {
                Coordinador coordinador = new Coordinador();
                coordinador.setRut("11.111.111-1");
                coordinador.setNombre("Prueba");
                coordinador.setApellido("Coordinador");
                coordinador.setCorreo("coordinador@ubb.cl");
                coordinador.setPassword(passwordEncoder.encode("admin"));
                coordinador.addRol(Rol.COORDINADOR);

                usuarioRepository.save(coordinador);
            } else {
                Usuario coord = coordOpt.get();
                if (coord.getRoles().isEmpty()) {
                    coord.addRol(Rol.COORDINADOR);
                    usuarioRepository.save(coord);
                }
            }

            Optional<Usuario> profOpt = usuarioRepository.findByCorreo("profesor@ubb.cl");
            if (profOpt.isEmpty()) {
                Profesor profesor = new Profesor();
                profesor.setRut("22.222.222-2");
                profesor.setNombre("Prueba");
                profesor.setApellido("Profesor");
                profesor.setCorreo("profesor@ubb.cl");
                profesor.setPassword(passwordEncoder.encode("profe"));
                profesor.addRol(Rol.PROFESOR_ASIGNATURA);
                profesor.addRol(Rol.TUTOR);

                usuarioRepository.save(profesor);
            } else {
                Usuario prof = profOpt.get();
                if (prof.getRoles().isEmpty()) {
                    prof.addRol(Rol.PROFESOR_ASIGNATURA);
                    prof.addRol(Rol.TUTOR);
                    usuarioRepository.save(prof);
                }
            }

            Optional<Usuario> estOpt = usuarioRepository.findByCorreo("estudiante@ubb.cl");
            if (estOpt.isEmpty()) {
                Estudiante estudiante = new Estudiante();
                estudiante.setRut("33.333.333-3");
                estudiante.setNombre("Prueba");
                estudiante.setApellido("Estudiante");
                estudiante.setCorreo("estudiante@ubb.cl");
                estudiante.setPassword(passwordEncoder.encode("estudiante"));
                estudiante.setAnoIngreso(2023);
                estudiante.setPracticaActual(1);
                estudiante.addRol(Rol.ESTUDIANTE);

                usuarioRepository.save(estudiante);
            } else {
                Usuario est = estOpt.get();
                if (est.getRoles().isEmpty()) {
                    est.addRol(Rol.ESTUDIANTE);
                    usuarioRepository.save(est);
                }
            }

            if (cargarDemo) {
                try {
                    datosDemo.cargar();
                } catch (RuntimeException e) {
                    // La carga demo es atómica: si algo falla no queda nada a medias y la app sigue arrancando
                    log.error("No se pudieron cargar los datos de demostración", e);
                }
            }
        };
    }

    // =====================================================================================
    //  DATOS DE DEMOSTRACIÓN
    //  Clave de todos los usuarios demo: demo1234
    //  Tokens de enlace fijos para probar el formulario del colaborador sin login:
    //    /evaluacion-colaborador/demo-vigente-sin-colaborador
    //    /evaluacion-colaborador/demo-vigente-con-colaborador
    //    /evaluacion-colaborador/demo-expirado
    //    /evaluacion-colaborador/demo-usado
    // =====================================================================================
    @Component
    public static class DatosDemo {

        private static final Logger log = LoggerFactory.getLogger(DatosDemo.class);
        private static final String PASSWORD_DEMO = "demo1234";
        private static final String CARPETA = "uploads/practicas/";

        @Autowired private UsuarioRepository usuarioRepository;
        @Autowired private ProfesorRepository profesorRepository;
        @Autowired private EstudianteRepository estudianteRepository;
        @Autowired private ColaboradorRepository colaboradorRepository;
        @Autowired private CentroPracticaRepository centroRepository;
        @Autowired private PracticaRepository practicaRepository;
        @Autowired private DocumentoRepository documentoRepository;
        @Autowired private PlanificacionRepository planificacionRepository;
        @Autowired private PautaRepository pautaRepository;
        @Autowired private EnlaceEvaluacionRepository enlaceRepository;
        @Autowired private NotificacionRepository notificacionRepository;
        @Autowired private InteraccionIARepository interaccionRepository;
        @Autowired private PasswordEncoder passwordEncoder;

        private String clave;

        @Transactional
        public void cargar() {
            // Marca de "ya cargado": no duplica datos en cada arranque
            if (usuarioRepository.findByCorreo("camila.rojas@ubb.cl").isPresent()) {
                log.info("Datos de demostración ya cargados; no se repite.");
                return;
            }
            clave = passwordEncoder.encode(PASSWORD_DEMO);
            LocalDateTime ahora = LocalDateTime.now();

            // ---------------------------------------------------------------- académicos
            Profesor base = profesorRepository.findById("22.222.222-2").orElse(null); // profesor@ubb.cl (PA + TUTOR)
            Profesor profA = profesor(15111222, "María", "González", "maria.gonzalez@ubb.cl", Rol.PROFESOR_ASIGNATURA);
            Profesor tutB = profesor(15222333, "Carlos", "Soto", "carlos.soto@ubb.cl", Rol.TUTOR);
            Profesor profC = profesor(15333444, "Andrea", "Vidal", "andrea.vidal@ubb.cl", Rol.PROFESOR_ASIGNATURA, Rol.TUTOR);
            profesor(15444555, "Rodrigo", "Pizarro", "rodrigo.pizarro@ubb.cl",
                    Rol.PROFESOR_ASIGNATURA, Rol.TUTOR, Rol.COORDINADOR); // 3 roles: prueba el selector
            if (base == null) base = profA;

            // ---------------------------------------------------------------- centros y colaboradores
            CentroPractica[] centros = {
                centro("Liceo Narciso Tondreau", "Ñuble", "Chillán", "Av. Libertad", "1234"),
                centro("Colegio Adventista de Chillán", "Ñuble", "Chillán", "Calle 5 de Abril", "560"),
                centro("Liceo Politécnico de San Carlos", "Ñuble", "San Carlos", "Calle Balmaceda", "320"),
                centro("Instituto Santa Cruz", "Ñuble", "Chillán Viejo", "Av. O'Higgins", "780")
            };
            Colaborador col1 = colaborador(12345678, "Patricia", "Núñez", "patricia.nunez@liceo.cl", "+56911111111");
            Colaborador col2 = colaborador(13456789, "Héctor", "Maldonado", "hector.maldonado@liceo.cl", "+56922222222");
            Colaborador col3 = colaborador(14567890, "Sandra", "Cifuentes", "sandra.cifuentes@liceo.cl", "+56933333333");
            Colaborador col4 = colaborador(16789012, "Ricardo", "Parra", "ricardo.parra@liceo.cl", "+56944444444");

            // ---------------------------------------------------------------- estudiante base (solo se le agregan datos)
            Estudiante estBase = estudianteRepository.findById("33.333.333-3").orElse(null);
            if (estBase != null) {
                Practica p = practica(estBase, 1, 2026, 2, EstadoPractica.PENDIENTE, null, base, profC, col1, centros[0]);
                plan(p, "Planificacion Practica 1 - Prueba Estudiante.pdf", ahora.minusDays(1));
                informe(p, "Informe parcial Practica 1 - Prueba Estudiante.pdf", ahora.minusHours(5));
                notif(estBase, "Bienvenido/a a la plataforma",
                        "Aquí recibirás los avisos de tus planificaciones y pautas.", null, true);
            }

            // ---------------------------------------------------------------- 1. Camila: casi todas las prácticas, notas altas
            Estudiante camila = estudiante(rut(20111222), "Camila", "Rojas", "camila.rojas@ubb.cl", 2021, 6, profA);
            aprobadas(camila, 1, 2023, 1, new double[]{6.1, 6.3, 5.8, 6.0, 6.5}, profA, tutB, col1, centros);
            Practica camilaP6 = practica(camila, 6, 2026, 2, EstadoPractica.PENDIENTE, null, profC, tutB, col1, centros[0]);
            plan(camilaP6, "Planificacion Practica 6 - Camila Rojas.pdf", ahora.minusDays(2));

            // ---------------------------------------------------------------- 2. Matías: base como profesor Y tutor (pautas del tutor)
            Estudiante matias = estudiante(rut(20222333), "Matías", "Pérez", "matias.perez@ubb.cl", 2022, 4, base);
            aprobadas(matias, 1, 2024, 1, new double[]{5.5, 5.9, 4.8}, base, tutB, col2, centros);
            Practica matiasP4 = practica(matias, 4, 2026, 2, EstadoPractica.PENDIENTE, null, base, base, col2, centros[1]);
            plan(matiasP4, "Planificacion Practica 4 - Matias Perez.pdf", ahora.minusDays(1));
            pautaOnline(matiasP4, true, base, null, "2° Medio B", LocalDate.now().minusDays(12), resp(3),
                    "Buena estructura de la clase. Se sugiere reforzar el cierre con una actividad de verificacion.");
            pautaOnline(matiasP4, false, base, null, "2° Medio B", LocalDate.now().minusDays(3), parcial(resp(5), 9),
                    "Borrador: falta completar los criterios de ambiente de aula.");
            pautaArchivo(matiasP4, "Pauta escaneada tutor - Matias Perez.pdf", base, ahora.minusDays(20));

            // ---------------------------------------------------------------- 3. Fernanda: reprobó P1 y repitió; tutor con 4 pautas (límite)
            Estudiante fernanda = estudiante(rut(20333444), "Fernanda", "Muñoz", "fernanda.munoz@ubb.cl", 2023, 2, profA);
            practicaCerrada(fernanda, 1, 2025, 1, 3.2, profA, tutB, col3, centros[2]);
            practicaCerrada(fernanda, 1, 2025, 2, 5.4, profA, tutB, col3, centros[2]);
            Practica fernandaP2 = practica(fernanda, 2, 2026, 2, EstadoPractica.PENDIENTE, null, profA, tutB, col3, centros[2]);
            plan(fernandaP2, "Planificacion Practica 2 - Fernanda Munoz.pdf", ahora.minusDays(4));
            for (int i = 1; i <= 4; i++) {
                pautaArchivo(fernandaP2, "Pauta " + i + " tutor - Fernanda Munoz.pdf", tutB, ahora.minusDays(30L - i));
            }

            // ---------------------------------------------------------------- 4. Sebastián: planificación pendiente de revisar (profesor base)
            Estudiante sebastian = estudiante(rut(21444555), "Sebastián", "Lagos", "sebastian.lagos@ubb.cl", 2024, 1, base);
            Practica sebastianP1 = practica(sebastian, 1, 2026, 2, EstadoPractica.PENDIENTE, null, base, profC, col1, centros[3]);
            plan(sebastianP1, "Planificacion Practica 1 - Sebastian Lagos.pdf", ahora.minusHours(8));

            // ---------------------------------------------------------------- 5. Valentina: confirmada, enlace VIGENTE, práctica SIN colaborador
            Estudiante valentina = estudiante(rut(21555666), "Valentina", "Araya", "valentina.araya@ubb.cl", 2024, 1, profA);
            Practica valentinaP1 = practica(valentina, 1, 2026, 2, EstadoPractica.PENDIENTE, null, profA, tutB, null, centros[0]);
            Planificacion valentinaPlan = plan(valentinaP1, "Planificacion Practica 1 - Valentina Araya.pdf", ahora.minusDays(3));
            confirmar(valentinaPlan, profA, ahora.minusDays(1));
            EnlaceEvaluacion enValentina = enlace("demo-vigente-sin-colaborador", valentinaPlan, valentinaP1,
                    ahora.minusDays(1), ahora.plusDays(6), false, null);
            avisoConfirmacion(valentina, valentinaPlan, valentinaP1, profA, enValentina, false);

            // ---------------------------------------------------------------- 6. Ignacio: confirmada, enlace VIGENTE, colaborador YA asignado
            Estudiante ignacio = estudiante(rut(22666777), "Ignacio", "Salazar", "ignacio.salazar@ubb.cl", 2025, 1, profA);
            Practica ignacioP1 = practica(ignacio, 1, 2026, 2, EstadoPractica.PENDIENTE, null, profA, profC, col4, centros[1]);
            Planificacion ignacioPlan = plan(ignacioP1, "Planificacion Practica 1 - Ignacio Salazar.pdf", ahora.minusDays(2));
            confirmar(ignacioPlan, profA, ahora.minusHours(10));
            EnlaceEvaluacion enIgnacio = enlace("demo-vigente-con-colaborador", ignacioPlan, ignacioP1,
                    ahora.minusHours(10), ahora.plusDays(7), false, null);
            avisoConfirmacion(ignacio, ignacioPlan, ignacioP1, profA, enIgnacio, false);

            // ---------------------------------------------------------------- 7. Joaquín: enlace EXPIRADO (el profesor base puede generar uno nuevo)
            Estudiante joaquin = estudiante(rut(20777888), "Joaquín", "Herrera", "joaquin.herrera@ubb.cl", 2023, 2, base);
            practicaCerrada(joaquin, 1, 2025, 2, 5.0, base, tutB, col2, centros[3]);
            Practica joaquinP2 = practica(joaquin, 2, 2026, 2, EstadoPractica.PENDIENTE, null, base, tutB, col2, centros[3]);
            Planificacion joaquinPlan = plan(joaquinP2, "Planificacion Practica 2 - Joaquin Herrera.pdf", ahora.minusDays(12));
            confirmar(joaquinPlan, base, ahora.minusDays(10));
            EnlaceEvaluacion enJoaquin = enlace("demo-expirado", joaquinPlan, joaquinP2,
                    ahora.minusDays(10), ahora.minusDays(3), false, null);
            avisoConfirmacion(joaquin, joaquinPlan, joaquinP2, base, enJoaquin, false);

            // ---------------------------------------------------------------- 8. Javiera: ciclo completo (enlace USADO, pauta colaborador y pautas del tutor base)
            Estudiante javiera = estudiante(rut(20888999), "Javiera", "Contreras", "javiera.contreras@ubb.cl", 2022, 3, profC);
            aprobadas(javiera, 1, 2024, 2, new double[]{5.7, 6.0}, profC, tutB, col3, centros);
            Practica javieraP3 = practica(javiera, 3, 2026, 2, EstadoPractica.PENDIENTE, null, profC, base, col3, centros[2]);
            Planificacion javieraPlan = plan(javieraP3, "Planificacion Practica 3 - Javiera Contreras.pdf", ahora.minusDays(8));
            confirmar(javieraPlan, profC, ahora.minusDays(6));
            Pauta pautaColab = pautaOnline(javieraP3, true, null, col3, "1° Medio C", LocalDate.now().minusDays(3), resp(2),
                    "Excelente trabajo colaborativo entre los estudiantes. Mejorar la gestion del tiempo.");
            EnlaceEvaluacion enJaviera = enlace("demo-usado", javieraPlan, javieraP3,
                    ahora.minusDays(6), ahora.plusDays(1), true, pautaColab);
            avisoConfirmacion(javiera, javieraPlan, javieraP3, profC, enJaviera, true);
            notif(javiera, "Tu profesor colaborador envió su pauta",
                    "Tu profesor colaborador " + col3.getNombre() + " " + col3.getApellido()
                            + " completó la pauta de evaluación de tu clase. Puedes revisarla en «Mis Pautas».", null, false);
            pautaOnline(javieraP3, true, base, null, "1° Medio C", LocalDate.now().minusDays(4), resp(4),
                    "Clase bien estructurada, con tareas contextualizadas al entorno de los estudiantes.");
            pautaOnline(javieraP3, false, base, null, "1° Medio C", LocalDate.now().minusDays(1), parcial(resp(1), 5),
                    "Segunda observacion en curso.");

            // ---------------------------------------------------------------- 9. Constanza: práctica SIN profesor -> revisa el coordinador (base)
            Estudiante constanza = estudiante(rut(22999111), "Constanza", "Bravo", "constanza.bravo@ubb.cl", 2025, 1, base);
            Practica constanzaP1 = practica(constanza, 1, 2026, 2, EstadoPractica.PENDIENTE, null, null, tutB, col4, centros[2]);
            plan(constanzaP1, "Planificacion Practica 1 - Constanza Bravo.pdf", ahora.minusHours(20));

            // ---------------------------------------------------------------- 10. Diego: sin prácticas (para asignar desde coordinación)
            //     Su planificación no tiene práctica: al confirmarla debe salir el mensaje de error.
            Estudiante diego = estudiante(rut(22111000), "Diego", "Navarro", "diego.navarro@ubb.cl", 2025, null, base);
            Planificacion suelta = new Planificacion();
            suelta.setEstudiante(diego);
            suelta.setNumeroPractica(1);
            suelta.setNombreArchivo("Planificacion sin practica - Diego Navarro.pdf");
            suelta.setRutaServidor(crearPdf("Planificacion sin practica - Diego Navarro"));
            suelta.setFechaSubida(ahora.minusDays(1));
            planificacionRepository.save(suelta);

            // ---------------------------------------------------------------- consultas al chatbot (para las analíticas)
            interaccion(camila, 6, "PLANIFICACIONES DE CLASE", "¿Cómo debo estructurar la planificación de mi clase?");
            interaccion(camila, 6, "INFORMES Y ENTREGAS", "¿Hasta cuándo puedo subir el informe final?");
            interaccion(matias, 4, "RÚBRICAS Y EVALUACIÓN", "¿Qué criterios evalúa la pauta del tutor?");
            interaccion(sebastian, 1, "DUDAS GENERALES DE PRÁCTICA", "¿Cuántas horas debo asistir al centro?");
            interaccion(valentina, 1, "PLANIFICACIONES DE CLASE", "¿Cuánto debe durar el inicio de la clase?");
            interaccion(valentina, 1, "CENTRO DE PRÁCTICA", "¿Qué hago si mi profesor colaborador no responde?");
            interaccion(joaquin, 2, "RÚBRICAS Y EVALUACIÓN", "¿Cómo se calcula la nota final de la práctica?");
            interaccion(ignacio, 1, "PLANIFICACIONES DE CLASE", "¿Puedo usar material digital en mi planificación?");
            interaccion(fernanda, 2, "DUDAS GENERALES DE PRÁCTICA", "¿Qué pasa si repruebo una práctica?");

            log.info("""

                    ===== DATOS DE DEMOSTRACIÓN CARGADOS (clave de todos: {}) =====
                    Profesores : maria.gonzalez@ubb.cl (PA) | carlos.soto@ubb.cl (Tutor) | andrea.vidal@ubb.cl (PA+Tutor)
                                 rodrigo.pizarro@ubb.cl (PA+Tutor+Coordinador)
                    Estudiantes: camila.rojas, matias.perez, fernanda.munoz, sebastian.lagos, valentina.araya,
                                 ignacio.salazar, joaquin.herrera, javiera.contreras, constanza.bravo, diego.navarro (@ubb.cl)
                    Enlaces    : /evaluacion-colaborador/demo-vigente-sin-colaborador | demo-vigente-con-colaborador
                                 | demo-expirado | demo-usado
                    ===============================================================""", PASSWORD_DEMO);
        }

        // =================================================================== usuarios y centros

        /** RUT con puntos y dígito verificador correcto (módulo 11), ej: 20111222 -> 20.111.222-K */
        private static String rut(int cuerpo) {
            int suma = 0;
            int mult = 2;
            for (int n = cuerpo; n > 0; n /= 10) {
                suma += (n % 10) * mult;
                mult = mult < 7 ? mult + 1 : 2;
            }
            int r = 11 - (suma % 11);
            String dv = r == 11 ? "0" : (r == 10 ? "K" : String.valueOf(r));
            String s = String.valueOf(cuerpo);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < s.length(); i++) {
                if (i > 0 && (s.length() - i) % 3 == 0) sb.append('.');
                sb.append(s.charAt(i));
            }
            return sb + "-" + dv;
        }

        private Profesor profesor(int cuerpoRut, String nombre, String apellido, String correo, Rol... roles) {
            Profesor p = new Profesor();
            p.setRut(rut(cuerpoRut));
            p.setNombre(nombre);
            p.setApellido(apellido);
            p.setCorreo(correo);
            p.setPassword(clave);
            p.getRoles().clear();
            for (Rol r : roles) p.addRol(r);
            return profesorRepository.save(p);
        }

        private Estudiante estudiante(String rut, String nombre, String apellido, String correo,
                                      int anoIngreso, Integer practicaActual, Profesor coordinador) {
            Estudiante e = new Estudiante();
            e.setRut(rut);
            e.setNombre(nombre);
            e.setApellido(apellido);
            e.setCorreo(correo);
            e.setPassword(clave);
            e.setAnoIngreso(anoIngreso);
            e.setPracticaActual(practicaActual);
            e.setProfesorCoordinador(coordinador);
            return estudianteRepository.save(e);
        }

        private CentroPractica centro(String nombre, String region, String comuna, String calle, String numero) {
            CentroPractica c = new CentroPractica();
            c.setNombre(nombre);
            c.setRegion(region);
            c.setComuna(comuna);
            c.setCalle(calle);
            c.setNumero(numero);
            return centroRepository.save(c);
        }

        private Colaborador colaborador(int cuerpoRut, String nombre, String apellido, String correo, String telefono) {
            Colaborador c = new Colaborador();
            c.setRut(rut(cuerpoRut));
            c.setNombre(nombre);
            c.setApellido(apellido);
            c.setCorreo(correo);
            c.setTelefono(telefono);
            return colaboradorRepository.save(c);
        }

        // =================================================================== prácticas

        private Practica practica(Estudiante e, int numero, int ano, int semestre, EstadoPractica estado, Double nota,
                                  Profesor profesor, Profesor tutor, Colaborador colaborador, CentroPractica centro) {
            Practica p = new Practica();
            p.setEstudiante(e);
            p.setNumeroPractica(numero);
            p.setAno(ano);
            p.setSemestre(semestre);
            p.setEstado(estado);
            p.setNotaAprobacion(nota);
            p.setFechaInicio(semestre == 1 ? LocalDate.of(ano, 3, 16) : LocalDate.of(ano, 8, 17));
            p.setFechaFin(semestre == 1 ? LocalDate.of(ano, 7, 17) : LocalDate.of(ano, 12, 18));
            p.setProfesor(profesor);
            p.setTutor(tutor);
            p.setColaborador(colaborador);
            p.setCentroPractica(centro);
            return practicaRepository.save(p);
        }

        /** Práctica terminada (APROBADA si nota >= 4.0, REPROBADA si no) con su informe final. */
        private Practica practicaCerrada(Estudiante e, int numero, int ano, int semestre, double nota,
                                         Profesor profesor, Profesor tutor, Colaborador col, CentroPractica centro) {
            EstadoPractica estado = nota >= 4.0 ? EstadoPractica.APROBADA : EstadoPractica.REPROBADA;
            Practica p = practica(e, numero, ano, semestre, estado, nota, profesor, tutor, col, centro);
            informe(p, "Informe final Practica " + numero + " " + ano + "-" + semestre + " - "
                    + e.getNombre() + " " + e.getApellido() + ".pdf",
                    LocalDateTime.of(ano, semestre == 1 ? 7 : 12, 10, 12, 0));
            return p;
        }

        /** Varias prácticas consecutivas (un semestre cada una) a partir de ano/semestre dados. */
        private void aprobadas(Estudiante e, int primerNumero, int anoInicio, int semInicio, double[] notas,
                               Profesor profesor, Profesor tutor, Colaborador col, CentroPractica[] centros) {
            for (int i = 0; i < notas.length; i++) {
                int idx = anoInicio * 2 + (semInicio - 1) + i;
                practicaCerrada(e, primerNumero + i, idx / 2, idx % 2 + 1, notas[i],
                        profesor, tutor, col, centros[i % centros.length]);
            }
        }

        // =================================================================== documentos

        private void rellenar(Documento d, Practica pr, String nombre, LocalDateTime fecha) {
            d.setPractica(pr);
            d.setEstudiante(pr.getEstudiante());
            d.setNumeroPractica(pr.getNumeroPractica());
            d.setNombreArchivo(nombre);
            d.setRutaServidor(crearPdf(nombre));
            d.setFechaSubida(fecha);
        }

        private Planificacion plan(Practica pr, String nombre, LocalDateTime fecha) {
            Planificacion d = new Planificacion();
            rellenar(d, pr, nombre, fecha);
            return planificacionRepository.save(d);
        }

        private void informe(Practica pr, String nombre, LocalDateTime fecha) {
            Informe d = new Informe();
            rellenar(d, pr, nombre, fecha);
            documentoRepository.save(d);
        }

        /** Pauta subida como archivo por el tutor (cuenta para el límite de 4). */
        private void pautaArchivo(Practica pr, String nombre, Profesor tutor, LocalDateTime fecha) {
            Pauta p = new Pauta();
            rellenar(p, pr, nombre, fecha);
            p.setTipoEvaluador(TipoEvaluador.TUTOR_UNIVERSIDAD);
            p.setProfesorEvaluador(tutor);
            pautaRepository.save(p);
        }

        /**
         * Pauta en línea. Con colaborador -> la envía el profesor colaborador (siempre enviada);
         * con tutor -> enviada o borrador.
         */
        private Pauta pautaOnline(Practica pr, boolean enviada, Profesor tutor, Colaborador colaborador,
                                  String curso, LocalDate fecha, int[] respuestas, String comentario) {
            Pauta p = new Pauta();
            p.setPractica(pr);
            p.setEstudiante(pr.getEstudiante());
            p.setNumeroPractica(pr.getNumeroPractica());
            p.setNombreArchivo((colaborador != null ? "Pauta Colaborador - " : "Pauta Online - ")
                    + curso + " (" + fecha + ")");
            p.setFechaSubida(fecha.atTime(18, 0));
            p.setRutaServidor(enviada ? "ONLINE_ENVIADA" : "ONLINE_BORRADOR");
            p.setPuntajeObtenido((double) Arrays.stream(respuestas).sum());
            p.setObservaciones(json(curso, fecha, "09:00", "10:30",
                    "Resolver problemas de proporcionalidad usando distintas estrategias.", comentario, respuestas));
            if (colaborador != null) {
                p.setTipoEvaluador(TipoEvaluador.PROFESOR_COLABORADOR);
                p.setColaboradorEvaluador(colaborador);
            } else {
                p.setTipoEvaluador(TipoEvaluador.TUTOR_UNIVERSIDAD);
                p.setProfesorEvaluador(tutor);
            }
            return pautaRepository.save(p);
        }

        /** Mismo formato JSON que usan TutorController y EvaluacionColaboradorService. */
        private String json(String curso, LocalDate fecha, String ini, String fin, String objetivo,
                            String comentario, int[] respuestas) {
            String arr = Arrays.stream(respuestas).boxed().map(String::valueOf).collect(Collectors.joining(","));
            return "{"
                    + "\"curso\":\"" + esc(curso) + "\","
                    + "\"fecha\":\"" + fecha + "\","
                    + "\"horaInicio\":\"" + ini + "\","
                    + "\"horaTermino\":\"" + fin + "\","
                    + "\"objetivo\":\"" + esc(objetivo) + "\","
                    + "\"comentario\":\"" + esc(comentario) + "\","
                    + "\"respuestas\":[" + arr + "]"
                    + "}";
        }

        private static String esc(String s) {
            return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
        }

        /** 20 criterios con valores entre 4 y 7; k cambia el patrón. */
        private static int[] resp(int k) {
            int[] r = new int[20];
            for (int i = 0; i < 20; i++) r[i] = 4 + ((i * k + i / 3) % 4);
            return r;
        }

        /** Deja solo los primeros n criterios respondidos (los demás en 0), como un borrador. */
        private static int[] parcial(int[] r, int n) {
            int[] c = new int[20];
            System.arraycopy(r, 0, c, 0, n);
            return c;
        }

        /** PDF mínimo de demostración para que "Revisar / Ver" abra algo real. */
        private String crearPdf(String titulo) {
            try {
                Path dir = Paths.get(CARPETA).toAbsolutePath().normalize();
                Files.createDirectories(dir);
                String nombre = "demo-" + UUID.randomUUID() + ".pdf";
                String texto = ("Documento de demostracion: " + titulo).replaceAll("[^A-Za-z0-9 ._:-]", "");
                String contenido = "BT /F1 11 Tf 15 70 Td (" + texto + ") Tj ET";
                String pdf = "%PDF-1.4\n"
                        + "1 0 obj<</Type/Catalog/Pages 2 0 R>>endobj\n"
                        + "2 0 obj<</Type/Pages/Kids[3 0 R]/Count 1>>endobj\n"
                        + "3 0 obj<</Type/Page/Parent 2 0 R/MediaBox[0 0 520 140]/Contents 4 0 R"
                        + "/Resources<</Font<</F1 5 0 R>>>>>>endobj\n"
                        + "4 0 obj<</Length " + contenido.length() + ">>stream\n" + contenido + "\nendstream endobj\n"
                        + "5 0 obj<</Type/Font/Subtype/Type1/BaseFont/Helvetica>>endobj\n"
                        + "trailer<</Root 1 0 R/Size 6>>\n%%EOF\n";
                Files.writeString(dir.resolve(nombre), pdf, StandardCharsets.ISO_8859_1);
                return CARPETA + nombre;
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        // =================================================================== revisión, enlaces y avisos

        private void confirmar(Planificacion p, Profesor revisor, LocalDateTime fecha) {
            p.setRevisada(true);
            p.setFechaRevision(fecha);
            p.setProfesorRevisor(revisor);
            p.setEvaluaConPautas(true);        // NUEVO
            p.setFechaMarcaPautas(fecha);      // NUEVO
            planificacionRepository.save(p);
        }

        private EnlaceEvaluacion enlace(String token, Planificacion plan, Practica pr, LocalDateTime creacion,
                                        LocalDateTime expiracion, boolean usado, Pauta pauta) {
            EnlaceEvaluacion en = new EnlaceEvaluacion();
            en.setToken(token);
            en.setPlanificacionId(plan.getId());
            en.setPractica(pr);
            en.setFechaCreacion(creacion);
            en.setFechaExpiracion(expiracion);
            en.setUsado(usado);
            if (usado) {
                en.setFechaUso(creacion.plusDays(2));
                en.setPautaId(pauta != null ? pauta.getId() : null);
            }
            return enlaceRepository.save(en);
        }

        private void avisoConfirmacion(Estudiante e, Planificacion plan, Practica pr, Profesor profesor,
                                       EnlaceEvaluacion enlace, boolean leida) {
            String quien = (profesor.getNombre() + " " + profesor.getApellido()).trim();
            notif(e, "Tu planificación fue confirmada",
                    "Tu planificación «" + plan.getNombreArchivo() + "» (Práctica N° " + pr.getNumeroPractica()
                            + ") fue confirmada por tu profesor " + quien + ". Ya puedes dar tu clase. "
                            + "Entrega el enlace de abajo a tu profesor colaborador para que complete su pauta de evaluación en línea.",
                    enlace, leida);
        }

        private void notif(Estudiante e, String titulo, String mensaje, EnlaceEvaluacion enlace, boolean leida) {
            Notificacion n = new Notificacion(e, titulo, mensaje, enlace);
            n.setLeida(leida);
            notificacionRepository.save(n);
        }

        private void interaccion(Estudiante e, int nivel, String categoria, String pregunta) {
            interaccionRepository.save(new InteraccionIA(e.getRut(), nivel, categoria, pregunta,
                    "Respuesta de demostración generada para pruebas."));
        }
    }
}