# Aprende a Aprender

Aplicación móvil Android para organización académica y refuerzo del aprendizaje. Permite administrar materias y tareas, sincronizar información académica de UTADEO, recibir notificaciones y realizar retos de estudio generados con Gemma ejecutado localmente en el dispositivo.

La persistencia principal utiliza PostgreSQL detrás de una API REST Spring Boot. Android no se conecta directamente a la base de datos y no contiene credenciales de PostgreSQL.

## Arquitectura actual

```text
                         +-----------------------+
                         |        UTADEO         |
                         |   sistema externo     |
                         +-----------+-----------+
                                     |
                                     | sincronización
                                     v
+---------------------+    HTTP/JSON + Bearer    +-----------------------+
|   Aplicación Android| ------------------------> | API Spring Boot       |
| Kotlin + Compose    |                           | Java 21 / JDBC        |
| WorkManager         | <------------------------ | Flyway / Security     |
+----------+----------+                           +-----------+-----------+
           |                                                  |
           | ejecución local                                  | JDBC
           v                                                  v
+---------------------+                           +-----------------------+
| Gemma / LiteRT-LM   |                           | PostgreSQL 16         |
+---------------------+                           +-----------------------+
```

## Funcionalidades principales

- Registro de usuarios nuevos.
- Inicio y cierre de sesión mediante la API propia.
- Gestión de perfil.
- Gestión de materias y temas.
- Gestión de tareas por prioridad y estado.
- Sincronización de materias, participantes y tareas de UTADEO.
- Persistencia del progreso de retos diarios.
- Generación local de preguntas mediante Gemma.
- Descarga del modelo y trabajos periódicos mediante WorkManager.
- Notificaciones de tareas y mensajes.

### Funcionalidades de autenticación aplazadas

La recuperación de contraseña y la verificación de correo electrónico se dejaron para una fase posterior. No son necesarias para ejecutar el sistema actual.

## Tecnologías

| Componente | Tecnología |
| --- | --- |
| Aplicación móvil | Kotlin, Jetpack Compose, Material 3 |
| Cliente HTTP | Retrofit 2 + OkHttp |
| Backend | Spring Boot 3.3.4 |
| JVM del backend | Java 21 |
| Persistencia | PostgreSQL 16 |
| Migraciones | Flyway |
| Seguridad | Spring Security, BCrypt, tokens Bearer con hash SHA-256 |
| IA local | Gemma + LiteRT-LM |
| Procesos Android | WorkManager |
| Entorno reproducible | Docker Compose |
| Carga experimental | k6 |
| CI | GitHub Actions |

# 1. Requisitos previos

Para la ruta recomendada de ejecución se requiere:

- Git.
- Docker Desktop o Docker Engine con `docker compose`.
- Android Studio.
- JDK 17 para el proyecto Android.
- Android SDK Platform 36 y Build-Tools 36.0.0.
- Dispositivo Android 8.0/API 26 o superior, físico o emulado.

Java 21 solo es obligatorio localmente si desea ejecutar el backend fuera de Docker. El contenedor ya incluye la versión necesaria.

Comprobaciones rápidas:

```bash
git --version
docker --version
docker compose version
java -version
```

# 2. Clonar el repositorio

```bash
git clone https://github.com/afat13/Proyecto-ArquitecturaSoftware.git
cd Proyecto-ArquitecturaSoftware
```

Para trabajar sobre la migración antes de fusionarla a `main`:

```bash
git switch migracion-postgresql
```

# 3. Variables de entorno

El repositorio contiene `.env.example`. Cree una copia llamada `.env` en la raíz.

### Windows PowerShell

```powershell
Copy-Item .env.example .env
```

### Linux/macOS

```bash
cp .env.example .env
```

Valores locales por defecto:

```dotenv
POSTGRES_DB=aprende_aprender
POSTGRES_USER=aprende
POSTGRES_PASSWORD=aprende_local
SESSION_HOURS=24
K6_VUS=30
K6_DURATION=60s
```

Estos valores son únicamente para desarrollo local. No reutilice la contraseña de ejemplo en un despliegue real.

# 4. Iniciar PostgreSQL y la API

Desde la raíz:

```bash
docker compose up -d --build db api
```

Comprobar estado:

```bash
docker compose ps
```

La base de datos debe aparecer como `healthy` y posteriormente la API también.

Comprobar el backend:

```text
http://localhost:8080/actuator/health
```

Respuesta esperada:

```json
{"status":"UP"}
```

Flyway ejecuta automáticamente las migraciones de `backend/src/main/resources/db/migration/` cuando inicia la API.

Para detener el entorno:

```bash
docker compose down
```

Para borrar también los datos locales de PostgreSQL y comenzar desde cero:

```bash
docker compose down -v
```

# 5. Ejecutar Android

## 5.1 Android SDK

Si aparece:

```text
SDK location not found
```

cree `local.properties` en la raíz. Ejemplo de Windows:

```properties
sdk.dir=C:\\Users\\USUARIO\\AppData\\Local\\Android\\Sdk
```

`local.properties` es local y no debe subirse al repositorio.

## 5.2 URL del backend

En un emulador Android estándar la aplicación usa por defecto:

```text
http://10.0.2.2:8080/
```

`10.0.2.2` permite al emulador acceder al `localhost` del computador anfitrión.

Para un teléfono físico conectado a la misma red, agregue en su `gradle.properties` local:

```properties
API_BASE_URL=http://IP_DEL_COMPUTADOR:8080/
```

Ejemplo conceptual:

```properties
API_BASE_URL=http://192.168.1.20:8080/
```

No copie una IP de ejemplo sin comprobar la IP real del computador.

## 5.3 Compilar

### Windows

```powershell
.\gradlew.bat assembleDebug
```

### Linux/macOS

```bash
./gradlew assembleDebug
```

También puede abrir el proyecto en Android Studio, seleccionar un dispositivo y ejecutar `app`.

# 6. Autenticación

El flujo implementado actualmente es:

```text
registro -> token Bearer -> sesión
login    -> token Bearer -> sesión
logout   -> eliminación de sesión
```

Las contraseñas se almacenan con BCrypt. El token de sesión se genera aleatoriamente y la base de datos conserva su hash SHA-256, no el token original.

Ejemplo de registro desde una terminal:

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"usuario@ejemplo.com","password":"Clave123!","firstName":"Usuario","lastName":"Prueba","phone":""}'
```

# 7. Pruebas automatizadas

## Backend

Requiere una instancia PostgreSQL disponible. La forma más simple es iniciar únicamente la base:

```bash
docker compose up -d db
```

Después:

```bash
gradle -p backend test
```

En GitHub Actions el job del backend inicia automáticamente PostgreSQL 16 antes de ejecutar las pruebas.

## Android

### Windows

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat lintDebug
.\gradlew.bat assembleDebug
```

### Linux/macOS

```bash
./gradlew testDebugUnitTest
./gradlew lintDebug
./gradlew assembleDebug
```

# 8. Integración continua

El workflow está en:

```text
.github/workflows/ci.yml
```

Ejecuta dos jobs independientes:

1. **Backend y PostgreSQL**: PostgreSQL 16, JDK 21, pruebas de integración y validación de Docker Compose.
2. **Android**: JDK 17, Android SDK 36, pruebas unitarias, lint y compilación del APK de depuración.

El workflow se ejecuta con `push` sobre `main` y `migracion-postgresql`, y en pull requests hacia `main`.

# 9. Experimento reproducible de línea base

La hipótesis preregistrada se encuentra en:

```text
docs/experimento/01-hipotesis-inicial.md
```

El experimento se encuentra en:

```text
experimentos/consulta-tareas/
```

Escenario inicial:

```text
Operación: GET /api/tasks
Semilla: 1 usuario, 8 materias, 10.000 tareas
Carga: 30 usuarios virtuales
Duración: 60 segundos por corrida
Corridas: 4
Corrida 1: calentamiento
Resultado: mediana del p95 de las corridas 2, 3 y 4
```

Para ejecutar la medición completa:

### Windows

```powershell
python .\experimentos\consulta-tareas\ejecutar_experimento.py
```

### Linux/macOS

```bash
python3 ./experimentos/consulta-tareas/ejecutar_experimento.py
```

El script crea o valida el usuario de prueba, carga la semilla, verifica su distribución, registra el hash del commit y las condiciones de ejecución, lanza cuatro corridas de k6 y conserva tanto los datos crudos como el resumen.

No se incluyen resultados inventados en el repositorio. Los números de línea base se agregan únicamente después de ejecutar el experimento.

# 10. Estructura relevante

```text
Proyecto-ArquitecturaSoftware/
├── app/                           # Aplicación Android
├── backend/                       # API Spring Boot
│   └── src/main/resources/
│       └── db/migration/          # Migraciones Flyway
├── dossier/                       # Contexto y dossier arquitectónico
├── docs/
│   └── experimento/               # Hipótesis y documentación experimental
├── experimentos/
│   └── consulta-tareas/           # Semilla, k6, ejecutor y resultados
├── .github/workflows/ci.yml       # CI
├── docker-compose.yml             # PostgreSQL + API + k6
└── .env.example                   # Variables locales de ejemplo
```

# 11. Persistencia y flujo de datos

La aplicación sigue la regla:

```text
Android -> API REST -> PostgreSQL
```

Nunca:

```text
Android -> PostgreSQL
```

La sincronización académica sigue el flujo:

```text
UTADEO -> Android -> API REST -> PostgreSQL
```

La generación de preguntas sigue siendo local:

```text
Android -> Gemma local
```

Cuando corresponde conservar preguntas/progreso:

```text
Gemma local -> repositorio Android -> API REST -> PostgreSQL
```

# 12. Seguridad y archivos que no deben versionarse

No suba al repositorio:

- `.env` con credenciales reales;
- `local.properties`;
- contraseñas de UTADEO;
- tokens de sesión;
- claves privadas o API keys personales;
- archivos locales del modelo si no están destinados expresamente a versionarse.

Las variables sensibles deben configurarse mediante variables de entorno o secretos del entorno de despliegue.

# 13. Estado de la migración

La arquitectura objetivo de este corte sustituye Firebase Authentication y Firebase Realtime Database por autenticación propia y PostgreSQL. Se crean usuarios nuevos; no se realiza migración de cuentas ni contraseñas históricas de Firebase.

La recuperación de contraseña y la verificación de correo quedan explícitamente aplazadas para una implementación posterior.
