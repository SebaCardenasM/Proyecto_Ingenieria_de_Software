
# Prácticas Pedagógicas

Plataforma web para la gestión de las prácticas de **Pedagogía en Educación Matemática**, Universidad del Bío-Bío.

---

## Descripción

Sistema centralizado para administrar las seis prácticas de la carrera: estudiantes, centros de práctica, profesores, tutores y colaboradores, junto con las planificaciones, informes y pautas de evaluación de cada una. Incorpora un asistente de IA (Gemini con RAG) que responde consultas y pre-revisa borradores antes de enviarlos.

## Funcionalidades

- **Panel por rol** (Estudiante, Profesor de Asignatura, Tutor Supervisor y Coordinador), con cambio de perfil para usuarios con varios roles.
- **Cadena de documentos:** borrador → revisión con el asistente IA → envío al gestor.
- **Planificación en línea** con formato diversificado (DUA) o subida de archivos (PDF, Word, etc.).
- **Revisión del profesor en dos pasos:** planificación revisada y clase evaluada con pautas, lo que genera un enlace temporal de un solo uso para el profesor colaborador.
- **Pautas de evaluación en línea** del tutor y del profesor colaborador (este último sin cuenta, por enlace), con dictado por voz.
- **Portafolio y notas** por estudiante, con exportación a CSV.
- **Asistente IA** con historial y monitoreo de consultas para coordinación y profesores.
- **Recuperación de contraseña** por correo con código de verificación.
- **Gestión de coordinación:** usuarios, prácticas, centros de práctica y profesores colaboradores.

## Tecnologías

- Java 21 y Spring Boot 4.1.1 (Web MVC, Data JPA, Security, Mail)
- Thymeleaf, Bootstrap 5 y Bootstrap Icons
- MySQL con Flyway
- LangChain4j con Google Gemini y embeddings locales (AllMiniLM-L6-v2)
- spring-dotenv para variables de entorno (carga de APIS mediante .env)

## Requisitos

| Herramienta | Versión / Detalle |
|---|---|
| JDK | 21 |
| Maven | 3.9+ |
| XAMPP | Con MySQL (MariaDB) |
| Editor | Visual Studio Code con *Extension Pack for Java* y *Spring Boot Extension Pack* |
| Navegador | Chrome, Edge (el dictado por voz usa la API de reconocimiento de voz del navegador), Brave queda excluido debido a errores con esta API|

Servicios externos opcionales para la configuracion del archivo (.env):

- Clave de **Google Gemini** para el asistente IA.
- Cuenta de **Gmail** con contraseña de aplicación para el envío de códigos de recuperación.

## Instalación y configuración

### 1. Clonar el repositorio

```bash
git clone https://github.com/SebaCardenasM/Proyecto_Ingenieria_de_Software.git
cd Proyecto_Ingenieria_de_Software/springboot-applications
```

### 2. Base de datos (XAMPP)

1. Abre el **XAMPP Control Panel** e inicia **MySQL**.
2. Entra a `http://localhost/phpmyadmin` (o a la consola de MySQL) y ejecuta:

```sql
CREATE DATABASE practicas_db CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
CREATE USER 'app_practicas'@'localhost' IDENTIFIED BY 'TU_CLAVE_SEGURA';
GRANT ALL PRIVILEGES ON practicas_db.* TO 'app_practicas'@'localhost';
FLUSH PRIVILEGES;
```

Las tablas se crean al iniciar la aplicación: Flyway aplica `db/migration/V1__esquema_inicial.sql` y Hibernate completa el esquema (`ddl-auto=update`).

### 3. Variables de entorno

Crea un archivo `.env` en `springboot-applications/`. Está pensado para no versionarse, así que verifica que figure en tu `.gitignore`.

```properties
DB_URL=jdbc:mysql://localhost:3306/practicas_db?serverTimezone=UTC
DB_USER=app_practicas
DB_PASSWORD=TU_CLAVE_SEGURA

GEMINI_API_KEY=tu_clave_de_gemini
GEMINI_MODEL=gemini-3.5-flash-lite

MAIL_USER=tu_correo@gmail.com
MAIL_PASSWORD=tu_contraseña_de_aplicación
```

### 4. Propiedades opcionales (`application.properties`)

| Propiedad | Descripción | Valor por defecto |
|---|---|---|
| `documentos.revision-ia.obligatoria` | Exige la revisión con la IA antes de enviar un documento al gestor | `false` |
| `evaluacion.enlace.dias` | Días de vigencia del enlace del profesor colaborador | `7` |
| `spring.servlet.multipart.max-file-size` | Tamaño máximo por archivo | `10MB` |

### 5. Documentos del asistente (RAG)

Coloca los PDF oficiales (rúbricas, pautas, programas de estudio) en `src/main/resources/rag-docs/`. Se indexan al iniciar para que el asistente responda con ese contexto.

### Perfiles

| Perfil | Uso | Comando |
|---|---|---|
| `dev` | Muestra SQL y logs de Flyway | `mvn spring-boot:run -Dspring-boot.run.profiles=dev` |
| `prod` | Conexión desde `DB_URL`, `DB_USER` y `DB_PASSWORD` | `mvn spring-boot:run -Dspring-boot.run.profiles=prod` |

## Ejecución

Desde `springboot-applications/`:

```bash
mvn spring-boot:run
```

También puedes ejecutarla desde VS Code abriendo `SpringbootApplicationsApplication.java` y pulsando **Run**, o con el *Spring Boot Dashboard*.

La aplicación queda disponible en **http://localhost:8080** y redirige a la pantalla de inicio de sesión. El acceso es con **RUT** y contraseña.

Para generar y ejecutar el `.jar`(debe tener instalado JDK, con el siguiente comando en la cmd: winget install Microsoft.OpenJDK.21)
ejecutar en la raiz del proyecto: \springboot-applications
```bash
mvnw clean package
java -jar target/springboot-applications-1.0.0.jar
```
Si da error, elimine de carpeta "target" dirección: \springboot-applications\target
y vuelva a ejecutar los codigos en \springboot-applications; (mvnw clean package ...)
## Pruebas

```bash
# Ejecutar las pruebas
mvn test

# Compilar, probar y verificar
mvn clean verify
```


## Roles del sistema

| Rol | Qué puede hacer |
|---|---|
| **Coordinador** | Gestiona usuarios, prácticas, centros y colaboradores; revisa documentos, notas y el monitoreo del asistente. |
| **Profesor de Asignatura** | Revisa planificaciones y entregas de sus estudiantes y consulta sus mensajes con el asistente. |
| **Tutor Supervisor** | Completa y envía pautas de observación de las prácticas que supervisa. |
| **Estudiante** | Crea planificaciones, sube informes, usa el asistente y consulta pautas, avisos y portafolio. |
| **Profesor Colaborador** | Completa su pauta mediante un enlace temporal, sin cuenta. |

## Solución de problemas

- **No conecta a la base de datos:** verifica que MySQL esté iniciado en XAMPP y que `DB_URL`, `DB_USER` y `DB_PASSWORD` coincidan con lo creado.
- **Puerto 8080 ocupado:** define `server.port=8081` en `application.properties`.
- **El asistente no responde:** revisa la API key que incluiste en el archivo .env `GEMINI_API_KEY`; .
- **No llegan los códigos de recuperación:** usa una contraseña de aplicación de Gmail en `MAIL_PASSWORD`, debes activar el 2FA en tu cuenta y crear una clave en google: Contraseñas de aplicación.
- **Podría Faltar la carpeta `uploads/`:** se crea al subir el primer archivo, en el directorio desde donde se ejecuta la aplicación.

