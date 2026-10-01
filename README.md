# 🦷 Dentora

Sistema web de gestión de pacientes, turnos y consultas para un centro odontológico.

Proyecto final de **Práctica Profesionalizante II** — Instituto Superior ICOP, Santa Fe.
Autor: **Francisco Cerliani**

---

## Tecnologías

| Capa | Tecnología |
|---|---|
| Lenguaje | Java 25 |
| Framework | Spring Boot 4 (Spring Web, Spring Security, Spring Data JPA) |
| Vistas | Thymeleaf + HTML, CSS y JavaScript |
| Base de datos | PostgreSQL 18 |
| Build | Maven (incluido en el proyecto, no hace falta instalarlo) |

---

## Requisitos

- **Java 25** (JDK). Verificar con `java -version`.
- **PostgreSQL**. La forma más simple es con **Docker Desktop**.
- Un editor: VS Code (con *Extension Pack for Java*) o IntelliJ IDEA.

---

## Cómo levantar el proyecto

### 1. Clonar el repositorio

```bash
git clone https://github.com/francerliani/dentora-sistema.git
cd dentora-sistema
```

### 2. Levantar PostgreSQL con Docker

Este comando crea un contenedor con PostgreSQL y una base vacía llamada `dentora`:

```bash
docker run --name dentora-db -e POSTGRES_USER=admin -e POSTGRES_PASSWORD=admin -e POSTGRES_DB=dentora -p 5432:5432 -d postgres:18
```

> Si ya tenés PostgreSQL instalado, alcanza con crear una base vacía llamada `dentora`.
> Si el puerto 5432 ya está ocupado, detené el otro PostgreSQL o cambiá el puerto.

### 3. Configurar las credenciales de la base

Por seguridad, el usuario y la contraseña de la base **no están en el repositorio**.
Crear el archivo `src/main/resources/secretos.properties` con estos datos (los mismos del paso 2, o los de tu PostgreSQL):

```properties
spring.datasource.username=admin
spring.datasource.password=admin
```

Este archivo está en `.gitignore`, así que nunca se sube a GitHub.

### 4. Ejecutar la aplicación

En Windows:

```bash
mvnw.cmd spring-boot:run
```

En Linux o macOS:

```bash
./mvnw spring-boot:run
```

También se puede ejecutar desde el editor, abriendo `DentoraApplication.java` y tocando **Run**.

La primera vez Maven descarga las dependencias, así que puede tardar unos minutos.
La aplicación está lista cuando en la consola aparece `Started DentoraApplication`.

### 5. Entrar al sistema

Abrir **http://localhost:8080**

Al arrancar por primera vez, el sistema crea automáticamente las tablas y estos usuarios de prueba:

| Usuario | Contraseña | Rol |
|---|---|---|
| `cperez` | `admin123` | Administrador |
| `lgomez` | `secre123` | Secretaria |
| `jgaite` | `odonto123` | Odontólogo |

Cada rol ve un menú distinto en la pantalla de inicio.

---

## Estructura del proyecto

```
src/main/java/com/dentora/dentora/
├── DentoraApplication.java   → punto de arranque
├── config/                   → seguridad (SecurityConfig) y datos iniciales (DataLoader)
├── controller/               → controladores web
├── model/                    → entidades JPA (una por tabla)
├── repository/               → acceso a datos (Spring Data JPA)
└── service/                  → lógica del sistema (UsuarioDetailsService)

src/main/resources/
├── application.properties    → configuración general
├── secretos.properties       → credenciales de la base (NO se sube, hay que crearlo)
├── static/                   → CSS e imágenes
└── templates/                → pantallas HTML (Thymeleaf)
```

---

## Estado del proyecto

- [x] Inicio de sesión con roles (Administrador, Secretaria, Odontólogo)
- [x] Contraseñas encriptadas con BCrypt
- [x] Menú según el rol del usuario
- [ ] Gestión de pacientes (en desarrollo)
- [ ] Gestión de turnos
- [ ] Odontograma y registro de consultas
- [ ] Configuración del sistema

---

## Problemas comunes

| Error en la consola | Solución |
|---|---|
| `Connection to localhost:5432 refused` | PostgreSQL no está corriendo. Abrir Docker Desktop e iniciar el contenedor. |
| `password authentication failed` | Revisar usuario y contraseña en `secretos.properties`. |
| `Port 8080 was already in use` | Ya hay otra ejecución abierta. Cerrarla y volver a ejecutar. |
