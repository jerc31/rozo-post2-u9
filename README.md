# Seguridad en Aplicaciones Web - Spring Boot + Spring Security 6 + MySQL

## Autor

- **Nombre:** Jhoseth Esneider Rozo Carrillo
- **Codigo:** 02230131027
- **Programa:** Ingenieria de Sistemas
- **Unidad:** 9 Seguridad en Aplicaciones Web
- **Actividad:** Post-Contenido 2
- **Fecha:** 05/05/2026

---

## Descripcion del Proyecto

Este proyecto extiende el Post-Contenido 1 de la Unidad 9. Ademas del login, registro, BCrypt, roles `ADMIN` y `USER`, ahora verifica protecciones activas de seguridad con autorizacion a nivel de metodo, mitigacion de XSS, cabecera Content-Security-Policy y proteccion CSRF.

### Funcionalidades Implementadas

**Post-Contenido 1 - Unidad 9:**

- Registro de usuarios con contrasenias hasheadas mediante BCrypt.
- Login personalizado con Spring Security 6.
- `UserDetailsService` consultando usuarios desde MySQL.
- Roles `ROLE_ADMIN` y `ROLE_USER`.
- Ruta `/admin` protegida para ADMIN.
- Logout con invalidacion de sesion.

**Post-Contenido 2 - Unidad 9:**

- `@PreAuthorize` en la capa de servicio.
- Expresiones SpEL distintas: `hasRole`, `hasAnyRole`, comparacion contra `authentication.name`.
- Vista personalizada para error `403`.
- Mitigacion de XSS mostrando datos de usuario con `th:text`.
- Cabecera `Content-Security-Policy` configurada.
- Verificacion de CSRF enviando POST sin token.

---

## Tecnologias Utilizadas

- **Spring Boot 3.2.5**: Framework principal
- **Spring Security 6**: Autenticacion, autorizacion, CSRF y cabeceras de seguridad
- **Spring Data JPA**: Acceso a datos
- **Hibernate 6.4.4**: Proveedor ORM
- **MySQL 8.x**: Base de datos relacional
- **Thymeleaf 3.1.2**: Motor de plantillas con escape HTML por defecto
- **Thymeleaf Extras Spring Security 6**: Control de vistas por rol
- **Jakarta Validation 3.0.2**: Validacion de formularios
- **Java 17**: Lenguaje de programacion
- **Maven 3.x**: Gestor de dependencias

---

## Estructura del Proyecto

- src/main/java/com/universidad/estudiantes/
- ├── EstudiantesApplication.java
- ├── config/
- │ └── SecurityConfig.java -> SecurityFilterChain, BCrypt, 403, CSP y reglas por rol
- ├── controller/
- │ ├── AuthController.java -> Login, registro, dashboard, panel admin y prueba PreAuthorize
- │ ├── ErrorController.java -> Vista personalizada `/error/403`
- ├── model/
- │ └── Usuario.java -> Entidad de autenticacion
- ├── repository/
- │ └── UsuarioRepository.java
- └── service/
- ├── UsuarioService.java -> Registro, consultas y metodos con `@PreAuthorize`
- └── UsuarioDetailsService.java

- src/main/resources/
- ├── application.properties
- └── templates/
- ├── auth/
- │ ├── login.html
- │ └── registro.html
- ├── admin/
- │ └── panel.html
- ├── error/
- │ └── 403.html
- └── dashboard.html

---

## Configuracion de la Base de Datos

### 1. Crear Base de Datos en MySQL

```bash
mysql -u root -p
```

Dentro de MySQL ejecutar:

```sql
CREATE DATABASE estudiantes_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'appuser'@'localhost' IDENTIFIED BY 'apppass';
GRANT ALL PRIVILEGES ON estudiantes_db.* TO 'appuser'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

### 2. Configurar application.properties

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/estudiantes_db?useSSL=false&serverTimezone=UTC
spring.datasource.username=appuser
spring.datasource.password=apppass
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.database-platform=org.hibernate.dialect.MySQL8Dialect

server.port=8080
```

---

## Instrucciones de Ejecucion

### 1. Ingresar a MySQL o MariaDB

```bash
C:\xampp\mysql\bin\mysql.exe -u root -p
```

### 2. Ejecutar la aplicacion

En PowerShell, en la carpeta del proyecto:

```bash
cd "C:\Users\Public\Dev\estudiantes"
.\mvnw.cmd spring-boot:run
```

Espera a ver en consola:

```text
Started EstudiantesApplication in X.XXX seconds
```

### 3. Acceder a la aplicacion

- Login: http://localhost:8080/login
- Registro: http://localhost:8080/registro
- Dashboard: http://localhost:8080/dashboard
- Panel ADMIN: http://localhost:8080/admin
- Prueba `@PreAuthorize`: http://localhost:8080/seguridad/usuarios

---

## Usuario ADMIN de Prueba

Primero ejecuta la aplicacion para que Hibernate cree la tabla `usuarios`. Luego inserta manualmente este usuario en MySQL.

La contrasenia en texto claro para la prueba es:

```text
admin123
```

```sql
INSERT INTO usuarios (nombre, email, contrasenia, rol, activo)
VALUES (
  'Administrador',
  'admin@universidad.edu',
  '$2a$12$QsdO.4Y7/URGB9hRHIPuqOTcnTj66Nof8OqrD0Kj06vX4LS6o9P6i',
  'ROLE_ADMIN',
  1
);
```

Usuarios de prueba:

- **ADMIN:** admin@universidad.edu / admin123
- **USER:** registrar desde `/registro` con cualquier correo y contrasenia.
- **USER XSS:** registrar desde `/registro` con nombre `<script>alert("XSS")</script>`.

---

## Seguridad Implementada

### Autorizacion con @PreAuthorize

En `UsuarioService` se agregaron metodos protegidos:

```java
@PreAuthorize("hasRole('ADMIN')")
public List<Usuario> listarTodos()
```

```java
@PreAuthorize("hasRole('ADMIN') or #email == authentication.name")
public Optional<Usuario> buscarPorEmail(String email)
```

```java
@PreAuthorize("hasAnyRole('ADMIN')")
public void cambiarRol(Long id, String nuevoRol)
```

```java
@PreAuthorize("#usuario.email == authentication.name or hasRole('ADMIN')")
public void actualizarNombre(Usuario usuario)
```

### Pagina 403 Personalizada

Cuando un usuario autenticado no tiene permisos, Spring Security redirige a:

```text
/error/403
```

La ruta acepta cualquier metodo HTTP para que los accesos denegados por CSRF en POST tambien respondan `403 Forbidden`.

La vista muestra el correo autenticado con:

```html
<strong sec:authentication="name"></strong>
```

### Mitigacion XSS con Thymeleaf

El dashboard muestra el nombre del usuario con `th:text`, que escapa HTML:

```html
<p>Nombre: <span th:text="${usuario.nombre}"></span></p>
```

No se usa `th:utext` con datos de usuario.

### Content-Security-Policy

La cabecera CSP se configura en `SecurityConfig`:

```text
default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline'; img-src 'self' data:; frame-ancestors 'none'
```

### CSRF

CSRF permanece activo por defecto en Spring Security. Los formularios Thymeleaf con `th:action` incluyen el token automaticamente. Un POST sin token debe responder `403 Forbidden`.

---

## CHECKPOINTS DE VERIFICACION

### Checkpoint 1 - @PreAuthorize y 403

1. Iniciar sesion como usuario USER.
2. Abrir:

```text
http://localhost:8080/seguridad/usuarios
```

3. El controlador llama `service.listarTodos()`.
4. `@PreAuthorize("hasRole('ADMIN')")` rechaza al usuario USER.
5. Debe mostrarse la vista personalizada `Acceso Denegado (403)` con el correo del usuario autenticado.

### Checkpoint 2 - XSS mitigado

1. Registrar un usuario con este nombre:

```html
<script>alert("XSS")</script>
```

2. Iniciar sesion con ese usuario.
3. Abrir:

```text
http://localhost:8080/dashboard
```

4. El dashboard debe mostrar el texto literal `<script>alert("XSS")</script>`.
5. No debe aparecer ningun `alert`.
6. En DevTools, inspeccionar el elemento `Nombre`; debe verse escapado como `&lt;script&gt;`.

### Checkpoint 3 - CSP y CSRF

Para verificar CSP:

1. Abrir Chrome DevTools con F12.
2. Ir a `Network`.
3. Recargar `/dashboard`.
4. Seleccionar la peticion `dashboard`.
5. En `Response Headers`, verificar:

```text
Content-Security-Policy: default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline'; img-src 'self' data:; frame-ancestors 'none'
```

Para verificar CSRF desde consola del navegador:

1. Iniciar sesion y abrir `/dashboard`.
2. Abrir DevTools -> Console.
3. Ejecutar:

```javascript
fetch("/logout", { method: "POST" })
  .then(r => console.log("Status:", r.status));
```

4. Resultado esperado:

```text
Status: 403
```

Tambien se puede verificar por curl:

```bash
curl -X POST http://localhost:8080/logout -v
```

Respuesta esperada:

```text
HTTP/1.1 403 Forbidden
```

---

## Capturas del Proyecto

Guarda las capturas requeridas en la carpeta `/evidencias/` con estos nombres:

- `post2_preauthorize_403.png`: error 403 personalizado al entrar a `/seguridad/usuarios` como USER.
- `post2_xss_escapado_dashboard.png`: dashboard mostrando `<script>alert("XSS")</script>` como texto.
- `post2_xss_html_escapado.png`: DevTools mostrando `&lt;script&gt;`.
- `post2_csp_header.png`: DevTools Network con `Content-Security-Policy`.
- `post2_csrf_403.png`: consola o curl mostrando status `403` al hacer POST sin token.

---

## Commits Requeridos

El repositorio incluye al menos 3 commits descriptivos para Post-Contenido 2:

- `feat: agregar autorizacion por metodo con preauthorize`
- `feat: agregar pagina personalizada para error 403`
- `feat: agregar csp y documentar pruebas xss csrf`
