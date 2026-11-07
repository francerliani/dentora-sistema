package com.dentora.dentora.config;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.dentora.dentora.model.Direccion;
import com.dentora.dentora.model.Disponibilidad;   // NUEVO
import com.dentora.dentora.model.EstadoTurno;      // NUEVO
import com.dentora.dentora.model.ObraSocial;
import com.dentora.dentora.model.Paciente;
import com.dentora.dentora.model.Rol;
import com.dentora.dentora.model.Sexo;
import com.dentora.dentora.model.Turno;            // NUEVO
import com.dentora.dentora.model.Usuario;
import com.dentora.dentora.repository.DisponibilidadRepository;  // NUEVO
import com.dentora.dentora.repository.EstadoTurnoRepository;     // NUEVO
import com.dentora.dentora.repository.ObraSocialRepository;
import com.dentora.dentora.repository.PacienteRepository;
import com.dentora.dentora.repository.RolRepository;
import com.dentora.dentora.repository.SexoRepository;
import com.dentora.dentora.repository.TurnoRepository;           // NUEVO
import com.dentora.dentora.repository.UsuarioRepository;

@Component
// Spring crea esta clase automáticamente al arrancar
public class DataLoader implements CommandLineRunner {
    // CommandLineRunner: el método run() se ejecuta UNA vez, apenas arranca el sistema

    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;
    private final SexoRepository sexoRepository;
    private final ObraSocialRepository obraSocialRepository;
    private final PacienteRepository pacienteRepository;
    private final EstadoTurnoRepository estadoTurnoRepository;          // NUEVO
    private final DisponibilidadRepository disponibilidadRepository;    // NUEVO
    private final TurnoRepository turnoRepository;                      // NUEVO
    private final PasswordEncoder passwordEncoder;

    // Inyección por constructor: Spring pasa todos los repositorios y el encriptador
    public DataLoader(RolRepository rolRepository,
                      UsuarioRepository usuarioRepository,
                      SexoRepository sexoRepository,
                      ObraSocialRepository obraSocialRepository,
                      PacienteRepository pacienteRepository,
                      EstadoTurnoRepository estadoTurnoRepository,          // NUEVO
                      DisponibilidadRepository disponibilidadRepository,    // NUEVO
                      TurnoRepository turnoRepository,                      // NUEVO
                      PasswordEncoder passwordEncoder) {
        this.rolRepository = rolRepository;
        this.usuarioRepository = usuarioRepository;
        this.sexoRepository = sexoRepository;
        this.obraSocialRepository = obraSocialRepository;
        this.pacienteRepository = pacienteRepository;
        this.estadoTurnoRepository = estadoTurnoRepository;
        this.disponibilidadRepository = disponibilidadRepository;
        this.turnoRepository = turnoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // Cargas INDEPENDIENTES: cada una revisa si sus datos ya existen.
        // El orden importa: cada una usa datos de las anteriores
        cargarRolesYUsuarios();
        cargarCatalogos();
        cargarPacientesDePrueba();
        cargarDisponibilidad();      // NUEVO: usa al odontólogo jgaite
        cargarTurnosDePrueba();      // NUEVO: usa pacientes, jgaite y los estados
    }                

    // =========================================================
    // 1. ROLES Y USUARIOS
    // =========================================================
    private void cargarRolesYUsuarios() {
        if (rolRepository.count() > 0) {
            return;   // ya están cargados: no hace nada
        }

        Rol administrador = rolRepository.save(new Rol("ADMINISTRADOR"));
        Rol secretaria    = rolRepository.save(new Rol("SECRETARIA"));
        Rol odontologo    = rolRepository.save(new Rol("ODONTOLOGO"));

        usuarioRepository.save(new Usuario("Carlos Pérez", "cperez",
                passwordEncoder.encode("admin123"), administrador));
        usuarioRepository.save(new Usuario("Laura Gómez", "lgomez",
                passwordEncoder.encode("secre123"), secretaria));
        usuarioRepository.save(new Usuario("Juan Gaite", "jgaite",
                passwordEncoder.encode("odonto123"), odontologo));

        System.out.println(">>> Roles y usuarios cargados");
    }

    // =========================================================
    // 2. CATÁLOGOS
    // =========================================================
    private void cargarCatalogos() {
        if (sexoRepository.count() == 0) {
            sexoRepository.save(new Sexo("Femenino"));
            sexoRepository.save(new Sexo("Masculino"));
            sexoRepository.save(new Sexo("Otro"));
            System.out.println(">>> Catálogo de sexos cargado");
        }

        if (obraSocialRepository.count() == 0) {
            obraSocialRepository.save(new ObraSocial("OSDE"));
            obraSocialRepository.save(new ObraSocial("IAPOS"));
            obraSocialRepository.save(new ObraSocial("Swiss Medical"));
            obraSocialRepository.save(new ObraSocial("PAMI"));
            obraSocialRepository.save(new ObraSocial("Galeno"));
            System.out.println(">>> Catálogo de obras sociales cargado");
        }
        // "Particular" NO se carga: un paciente sin obra social simplemente la tiene en null

        // NUEVO: estados del turno (catálogo fijo, RN-09 y RN-13)
        if (estadoTurnoRepository.count() == 0) {
            estadoTurnoRepository.save(new EstadoTurno("Pendiente"));
            estadoTurnoRepository.save(new EstadoTurno("Atendido"));
            estadoTurnoRepository.save(new EstadoTurno("Cancelado"));
            System.out.println(">>> Catálogo de estados de turno cargado");
        }
    }

    // =========================================================
    // 3. PACIENTES DE PRUEBA
    // =========================================================
    private void cargarPacientesDePrueba() {
        if (pacienteRepository.count() > 0) {
            return;
        }

        Sexo femenino  = sexoRepository.findAll().get(0);
        Sexo masculino = sexoRepository.findAll().get(1);
        ObraSocial osde  = obraSocialRepository.findAll().get(0);
        ObraSocial iapos = obraSocialRepository.findAll().get(1);

        pacienteRepository.save(crearPaciente("María", "González", "30456789",
                LocalDate.of(1985, 3, 12), "342 555-1234", femenino, osde,
                new Direccion("San Martín", 2450, "Santa Fe")));

        pacienteRepository.save(crearPaciente("Lucas", "Fernández", "35123456",
                LocalDate.of(1990, 7, 25), "342 555-9876", masculino, iapos,
                new Direccion("Bv. Pellegrini", 1300, "Santa Fe")));

        pacienteRepository.save(crearPaciente("Ana", "Rodríguez", "28987654",
                LocalDate.of(1980, 11, 3), "342 555-4567", femenino, null,   // null = Particular
                new Direccion("Rivadavia", 3100, "Santa Fe")));

        System.out.println(">>> Pacientes de prueba cargados");
    }

    // =========================================================
    // 4. NUEVO: DISPONIBILIDAD del odontólogo de prueba
    // =========================================================
    private void cargarDisponibilidad() {
        if (disponibilidadRepository.count() > 0) {
            return;
        }

        // Buscamos a jgaite (el odontólogo de prueba)
        Usuario jgaite = usuarioRepository.findByUsuario("jgaite").orElseThrow();

        // Atiende de lunes a viernes, de 15:00 a 20:00 → 5 bloques de 1 hora por día
        List<String> dias = List.of("LUNES", "MARTES", "MIERCOLES", "JUEVES", "VIERNES");
        for (String dia : dias) {
            disponibilidadRepository.save(new Disponibilidad(dia,
                    LocalTime.of(15, 0), LocalTime.of(20, 0), jgaite));
        }

        System.out.println(">>> Disponibilidad de jgaite cargada");
    }

    // =========================================================
    // 5. NUEVO: TURNOS DE PRUEBA (los mismos de la maqueta)
    // =========================================================
    private void cargarTurnosDePrueba() {
        if (turnoRepository.count() > 0) {
            return;
        }

        Usuario jgaite = usuarioRepository.findByUsuario("jgaite").orElseThrow();
        EstadoTurno pendiente = estadoTurnoRepository.findByNombre("Pendiente").orElseThrow();

        // Los 3 pacientes de prueba (son los primeros que se cargaron)
        List<Paciente> pacientes = pacienteRepository.findAll();

        // Elegimos el próximo día hábil (si hoy es sábado o domingo, pasa al lunes),
        // porque jgaite no atiende los fines de semana
        LocalDate dia = LocalDate.now();
        while (dia.getDayOfWeek() == DayOfWeek.SATURDAY || dia.getDayOfWeek() == DayOfWeek.SUNDAY) {
            dia = dia.plusDays(1);
        }

        // 15:00 González (1 h) · 16:00 a 18:00 Fernández (2 h) · 18:00 libre · 19:00 Rodríguez (1 h)
        turnoRepository.save(crearTurno(dia, LocalTime.of(15, 0), 1, pacientes.get(0), jgaite, pendiente));
        turnoRepository.save(crearTurno(dia, LocalTime.of(16, 0), 2, pacientes.get(1), jgaite, pendiente));
        turnoRepository.save(crearTurno(dia, LocalTime.of(19, 0), 1, pacientes.get(2), jgaite, pendiente));

        System.out.println(">>> Turnos de prueba cargados para el " + dia);
    }

    // ===== Métodos auxiliares =====

    private Paciente crearPaciente(String nombre, String apellido, String dni,
                                   LocalDate fechaNacimiento, String telefono,
                                   Sexo sexo, ObraSocial obraSocial, Direccion direccion) {
        Paciente p = new Paciente();
        p.setNombre(nombre);
        p.setApellido(apellido);
        p.setDni(dni);
        p.setFechaNacimiento(fechaNacimiento);
        p.setTelefono(telefono);
        p.setSexo(sexo);
        p.setObraSocial(obraSocial);
        p.setDireccion(direccion);
        return p;
    }

    // NUEVO: arma un Turno (mismo motivo que crearPaciente: no repetir setters)
    private Turno crearTurno(LocalDate fecha, LocalTime hora, int duracionHoras,
                             Paciente paciente, Usuario odontologo, EstadoTurno estado) {
        Turno t = new Turno();
        t.setFecha(fecha);
        t.setHora(hora);
        t.setDuracionHoras(duracionHoras);
        t.setPaciente(paciente);
        t.setOdontologo(odontologo);
        t.setEstado(estado);
        return t;
    }
}