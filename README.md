# Aprende a Aprender

Aplicación móvil Android para organización académica y refuerzo del aprendizaje. Permite administrar materias y tareas, sincronizar información académica de UTADEO, recibir notificaciones y realizar retos de estudio generados con Gemma ejecutado localmente en el dispositivo.

La persistencia principal utiliza PostgreSQL detrás de una API REST Spring Boot. Android no se conecta directamente a la base de datos y no contiene credenciales de PostgreSQL.

## Arquitectura actual

```text
UTADEO -> Android -> API REST Spring Boot -> PostgreSQL 16
                 \
                  -> Gemma local en el dispositivo
```

Regla principal de persistencia:

```text
Android -> API REST -> PostgreSQL
```

## Funcionalidades principales

- Registro, inicio y cierre de sesión mediante API propia.
- Gestión de perfil.
- Gestión de materias, temas y tareas.
- Sincronización académica con UTADEO.
- Persistencia del progreso de retos diarios.
- Generación local de preguntas mediante Gemma.
- WorkManager para trabajos periódicos y descarga del modelo.
- Notificaciones de tareas y mensajes.

La recuperación de contraseña y la verificación de correo quedan aplazadas para una fase posterior.

## Tecnologías

| Componente | Tecnología |
| --- | --- |
| Aplicación móvil | Kotlin, Jetpack Compose, Material 3 |
| Cliente HTTP | Retrofit 2 + OkHttp |
| Backend | Spring Boot 3.3.4 |
| JVM backend | Java 21 |
| Persistencia | PostgreSQL 16 |
| Migraciones | Flyway |
| Seguridad | Spring Security, BCrypt, tokens Bearer con hash SHA-256 |
| IA local | Gemma + LiteRT-LM |
| Procesos Android | WorkManager |
| Reproducibilidad | Docker Compose |
| Carga experimental | k6 |
| CI | GitHub Actions |

# 1. Requisitos previos

Ruta recomendada:

- Git.
- Docker Desktop o Docker Engine con `docker compose`.
- Android Studio.
- JDK 17 para Android.
- Android SDK Platform 36 y Build-Tools 36.0.0.
- Dispositivo Android 8.0/API 26 o superior, físico o emulado.

Java 21 solo es necesario localmente si el backend se ejecuta fuera de Docker.

# 2. Clonar el repositorio

```bash
git clone https://github.com/afat13/Proyecto-ArquitecturaSoftware.git
cd Proyecto-ArquitecturaSoftware
```

La rama `main` contiene la versión integrada y actual del proyecto.

# 3. Variables de entorno

Copiar `.env.example` como `.env`.

Windows PowerShell:

```powershell
Copy-Item .env.example .env
```

Linux/macOS:

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

No reutilizar estas credenciales de ejemplo en producción.

# 4. Iniciar PostgreSQL y API

```bash
docker compose up -d --build db api
docker compose ps
```

Comprobar:

```text
http://localhost:8080/actuator/health
```

Respuesta esperada:

```json
{"status":"UP"}
```

Flyway ejecuta automáticamente las migraciones ubicadas en `backend/src/main/resources/db/migration/`.

Detener:

```bash
docker compose down
```

Eliminar también los datos locales:

```bash
docker compose down -v
```

# 5. Ejecutar Android

Si aparece `SDK location not found`, crear `local.properties` con la ruta real del SDK. Ejemplo Windows:

```properties
sdk.dir=C:\\Users\\USUARIO\\AppData\\Local\\Android\\Sdk
```

`local.properties` no debe versionarse.

En emulador Android, la API usa por defecto:

```text
http://10.0.2.2:8080/
```

Para teléfono físico en la misma red, configurar localmente:

```properties
API_BASE_URL=http://IP_DEL_COMPUTADOR:8080/
```

Compilar Android:

Windows:

```powershell
.\gradlew.bat assembleDebug
```

Linux/macOS:

```bash
./gradlew assembleDebug
```

# 6. Autenticación

Flujo implementado:

```text
registro -> token Bearer -> sesión
login    -> token Bearer -> sesión
logout   -> eliminación de sesión
```

Las contraseñas se almacenan con BCrypt. El token de sesión se genera aleatoriamente y PostgreSQL conserva únicamente su hash SHA-256.

# 7. Pruebas automatizadas

Backend:

```bash
docker compose up -d db
gradle -p backend test
```

Android en Windows:

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat lintDebug
.\gradlew.bat assembleDebug
```

Android en Linux/macOS:

```bash
./gradlew testDebugUnitTest
./gradlew lintDebug
./gradlew assembleDebug
```

# 8. Integración continua

Workflow:

```text
.github/workflows/ci.yml
```

Jobs principales:

1. Backend y PostgreSQL: PostgreSQL 16, JDK 21, pruebas, Docker Compose y validación del experimento.
2. Android: JDK 17, Android SDK 36, pruebas unitarias, lint y compilación APK.

Se ejecuta con push sobre `main` y `migracion-postgresql`, y en pull requests hacia `main` o `migracion-postgresql`.

# 9. Experimento reproducible de línea base

La hipótesis inicial preregistrada se conserva en:

```text
docs/experimento/01-hipotesis-inicial.md
```

El diseño experimental finalmente medido utiliza una semilla multiusuario reproducible:

```text
Operación: GET /api/tasks
Usuarios de la semilla: 5.000
Cuentas distintas: 5.000
Materias por usuario: 5
Tareas por usuario: 1.000
Total de materias: 25.000
Total de tareas: 5.000.000
Carga: 30 usuarios virtuales
Identidades k6: una cuenta distinta por VU
Duración: 60 segundos por corrida
Corridas: 4
Corrida 1: calentamiento
Resultado: mediana del p95 de las corridas 2, 3 y 4
```

La verificación real de la semilla confirmó:

```text
usuarios: 5.000
correos únicos: 5.000
materias: 25.000
tareas: 5.000.000
mínimo tareas/usuario: 1.000
máximo tareas/usuario: 1.000
promedio tareas/usuario: 1.000
usuarios con exactamente 1.000 tareas: 5.000
```

La línea base obtenida para `GET /api/tasks` fue:

```text
Corrida 1: p95 135,74 ms  (calentamiento, descartada)
Corrida 2: p95 103,72 ms
Corrida 3: p95  90,75 ms
Corrida 4: p95  82,93 ms

Línea base = mediana del p95 de corridas 2–4 = 90,75 ms
```

Las cuatro corridas reportaron cero checks fallidos.

Los archivos del experimento están en:

```text
experimentos/consulta-tareas/
```

La evidencia cruda se conserva en:

```text
experimentos/consulta-tareas/resultados/
```

Incluye `contexto.json`, `verificacion-semilla.csv`, los cuatro JSON y logs de k6 y `resultado.json`.

Ejecutar el experimento completo en Windows:

```powershell
python .\experimentos\consulta-tareas\ejecutar_experimento.py
```

Linux/macOS:

```bash
python3 ./experimentos/consulta-tareas/ejecutar_experimento.py
```

El ejecutor construye PostgreSQL y la API, carga y verifica la semilla, registra el hash del commit y las condiciones de máquina, ejecuta cuatro corridas y conserva las salidas originales.

# 10. Estructura relevante

```text
Proyecto-ArquitecturaSoftware/
├── app/                           # Aplicación Android
├── backend/                       # API Spring Boot
│   └── src/main/resources/db/migration/
├── dossier/                       # Documentación arquitectónica y registros de IA
├── docs/experimento/              # Hipótesis y documentación experimental
├── experimentos/consulta-tareas/  # Semilla, k6, ejecutor y resultados
├── .github/workflows/ci.yml       # CI
├── docker-compose.yml             # PostgreSQL + API + k6
└── .env.example
```

En `dossier/` también se conservan los registros críticos de sugerencias de IA y la reformulación de escenarios de calidad:

- `dossier/07-registro-critico-ia-riesgos.md`;
- `dossier/08-registro-ia-escenarios-calidad.md`.

# 11. Seguridad

No versionar:

- `.env` con credenciales reales;
- `local.properties`;
- contraseñas de UTADEO;
- tokens de sesión;
- claves privadas o API keys personales.

Las variables sensibles deben configurarse mediante variables de entorno o secretos del entorno de despliegue.

# 12. Estado de la migración

La arquitectura de este corte sustituye Firebase Authentication y Firebase Realtime Database por autenticación propia y PostgreSQL. Los usuarios se crean nuevamente; no se realiza migración de cuentas ni contraseñas históricas de Firebase.

La recuperación de contraseña y la verificación de correo quedan aplazadas para una implementación posterior.
