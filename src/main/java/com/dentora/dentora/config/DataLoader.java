package com.dentora.dentora.config;

import java.time.LocalDate;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.dentora.dentora.model.Direccion;
import com.dentora.dentora.model.ObraSocial;
import com.dentora.dentora.model.Paciente;
import com.dentora.dentora.model.Rol;
import com.dentora.dentora.model.Sexo;
import com.dentora.dentora.model.Usuario;
import com.dentora.dentora.repository.ObraSocialRepository;
import com.dentora.dentora.repository.PacienteRepository;
import com.dentora.dentora.repository.RolRepository;
import com.dentora.dentora.repository.SexoRepository;
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
    private final PasswordEncoder passwordEncoder;

    // Inyección por constructor: Spring pasa todos los repositorios y el encriptador
    public DataLoader(RolRepository rolRepository,
                      UsuarioRepository usuarioRepository,
                      SexoRepository sexoRepository,
                      ObraSocialRepository obraSocialRepository,
                      PacienteRepository pacienteRepository,
                      PasswordEncoder passwordEncoder) {
        this.rolRepository = rolRepository;
        this.usuarioRepository = usuarioRepository;
        this.sexoRepository = sexoRepository;
        this.obraSocialRepository = obraSocialRepository;
        this.pacienteRepository = pacienteRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // Tres cargas INDEPENDIENTES: cada una revisa si sus datos ya existen.
        // Así, agregar algo nuevo no depende de lo que ya se cargó antes
        cargarRolesYUsuarios();
        cargarCatalogos();
        cargarPacientesDePrueba();   // va último: los pacientes usan los catálogos
    }

    // =========================================================
    // 1. ROLES Y USUARIOS (lo que ya teníamos)
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
    // 2. CATÁLOGOS: opciones de los desplegables del formulario
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
    }

    // =========================================================
    // 3. PACIENTES DE PRUEBA: para tener algo que mostrar en el listado
    // =========================================================
    private void cargarPacientesDePrueba() {
        if (pacienteRepository.count() > 0) {
            return;
        }

        // Buscamos los catálogos recién cargados para asignarlos a los pacientes.
        // findAll() devuelve una lista; .get(0) es el primer elemento, .get(1) el segundo...
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

        // Gracias a cascade = ALL en Paciente, cada save() guarda también la dirección

        System.out.println(">>> Pacientes de prueba cargados");
    }

    // Método auxiliar: arma un Paciente con todos sus datos.
    // Existe para no repetir 9 setters por cada paciente de arriba
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
}