# 🦷 Dentora

Sistema web para la gestión de un consultorio odontológico: pacientes, agenda de turnos por profesional, odontograma interactivo con numeración FDI, historial clínico y reportes de actividad y facturación. El acceso está controlado por roles (Administrador, Secretaria y Odontólogo).

Proyecto final de **Práctica Profesionalizante II** — Tecnicatura Superior en Desarrollo de Software, Instituto Superior ICOP (Santa Fe, Argentina).
Autor: **Francisco Cerliani**

![Odontograma](docs/capturas/odontograma.png)

---

## Funcionalidades

**Pacientes**
- Alta, modificación y baja lógica, con validación de DNI único y reactivación de pacientes dados de baja.
- Búsqueda por DNI o apellido y filtro por obra social.
- Consulta de los próximos turnos y del historial de turnos de cada paciente.

**Turnos**
- Agenda por odontólogo y fecha, en bloques de 1 hora calculados según la disponibilidad horaria de cada profesional.
- Turnos de 1 a 4 horas, con control de superposiciones del odontólogo y del paciente.
- Asignar, reprogramar, cancelar y marcar como ausente.

**Odontograma y consultas**
- Odontograma de 32 piezas con numeración FDI y selección por cara (oclusal, vestibular, palatina/lingual, mesial y distal).
- Registro de consultas con tratamientos por pieza y cara; estados *A realizar* (azul) y *Realizado* (rojo).
- Autocompletado de tratamientos pendientes al seleccionar una cara ya diagnosticada.
- Historial clínico con filtros por fecha y por pieza, y exportación de la historia clínica a PDF.

**Reportes**
- Turnos atendidos por odontólogo, pacientes por obra social y facturación por odontólogo, por mes o por año.
- Cada odontólogo ve sus propios reportes; el Administrador ve los de todo el centro.

**Seguridad y configuración**
- Inicio de sesión con Spring Security, contraseñas con BCrypt y bloqueo de la cuenta tras 5 intentos fallidos.
- Menú y permisos según el rol, verificados también en el servidor.
- Cambio de contraseña por el propio usuario y páginas de error propias.
- Administración de usuarios, disponibilidad horaria, obras sociales y tratamientos con su precio.

---

## Capturas

| Agenda de turnos | Historial clínico |
|---|---|
| ![Agenda](docs/capturas/agenda.png) | ![Historial](docs/capturas/historial.png) |

| Pacientes | Turnos del paciente |
|---|---|
| ![Pacientes](docs/capturas/pacientes.png) | ![Turnos del paciente](docs/capturas/turnos-paciente.png) |

| Inicio del odontólogo | Reporte de facturación |
|---|---|
| ![Inicio](docs/capturas/inicio.png) | ![Reportes](docs/capturas/reportes.png) |

---

## Tecnologías

| Parte | Tecnología |
|---|---|
| Lenguaje | Java 25 |
| Framework | Spring Boot 4 (Spring Web MVC, Spring Security, Spring Data JPA con Hibernate) |
| Vistas | Thymeleaf, HTML, CSS y JavaScript (odontograma en SVG) |
| Base de datos | PostgreSQL 18 (en Docker) |
| PDF | OpenPDF |
| Build | Maven (incluido en el proyecto con el wrapper `mvnw`) |

**Arquitectura:** MVC en capas — `model` (entidades JPA), `repository` (Spring Data), `service` (reglas de negocio), `controller` (web) y `config` (seguridad y datos iniciales).

---

## Cómo ejecutarlo

### 1. Clonar el repositorio

```bash
git clone https://github.com/francerliani/dentora-sistema.git
cd dentora-sistema
```

### 2. Levantar PostgreSQL con Docker

```bash
docker run --name dentora-db -e POSTGRES_USER=admin -e POSTGRES_PASSWORD=admin -e POSTGRES_DB=dentora -p 5432:5432 -d postgres:18
```

> Si ya tenés PostgreSQL instalado, alcanza con crear una base vacía llamada `dentora`.

### 3. Configurar las credenciales de la base

Las credenciales no están en el repositorio. Crear el archivo `src/main/resources/secretos.properties`:

```properties
spring.datasource.username=admin
spring.datasource.password=admin
```

### 4. Ejecutar

```bash
# Windows
mvnw.cmd spring-boot:run

# Linux / macOS
./mvnw spring-boot:run
```

Abrir **http://localhost:8080**. Al primer inicio se crean las tablas, los catálogos y los usuarios de prueba.

### Usuarios de prueba

| Usuario | Contraseña | Rol |
|---|---|---|
| `cperez` | `admin123` | Administrador |
| `lgomez` | `secre123` | Secretaria |
| `jgaite` | `odonto123` | Odontólogo |

---

## Estructura

```
src/main/java/com/dentora/dentora/
├── config/       → seguridad, datos iniciales y datos comunes a las vistas
├── controller/   → controladores web
├── model/        → entidades JPA
├── repository/   → acceso a datos
└── service/      → reglas de negocio (agenda, consultas, odontograma, reportes)

src/main/resources/
├── templates/    → pantallas Thymeleaf
├── static/       → CSS, JavaScript e imágenes
└── application.properties
```
