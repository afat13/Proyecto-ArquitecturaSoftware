# Vistas arquitectónicas 4+1 — Aprende a Aprender

**Módulo:** 7  
**Estado:** vistas documentales basadas en código de `main`; no implican nuevas funcionalidades implementadas  
**Fecha:** 6 de noviembre de 2026

## Alcance y relación con C4

El modelo **4+1** complementa los diagramas C4 existentes de contexto, contenedores y componentes en `docs/c4/`. No los reemplaza: el 4+1 explica el sistema desde cinco preguntas distintas. Las vistas describen principalmente lo implementado; cualquier mejora aún no desarrollada queda señalada como propuesta.

## 1. Vista lógica — responsabilidades del sistema

Separamos las responsabilidades en el cliente Android y el backend. El cliente controla la navegación, presentación, autenticación local de la sesión y orquestación de repositorios; Spring Boot aplica autenticación, reglas de acceso por usuario y persistencia; PostgreSQL guarda usuarios, materias, tareas y retos.

```mermaid
flowchart LR
    Estudiante[Estudiante] --> UI[Pantallas Android Compose]
    UI --> VM[ViewModels]
    VM --> Repo[Repositorios Android]
    Repo --> Retrofit[ApiService y ApiClient]
    Retrofit --> API[Controladores Spring Boot]
    API --> Auth[AuthService y filtro Bearer]
    API --> JDBC[JdbcClient]
    JDBC --> DB[(PostgreSQL)]
    Repo --> Modelo[Gemma local y caché de preguntas]
    Repo --> Utadeo[Sistema académico UTADEO]
```

**Decisión de diseño observada:** el control de propiedad de las tareas se encuentra en las consultas del backend mediante `user_id`; no se delega al cliente Android. Gemma trabaja localmente y no constituye un microservicio remoto. La conexión de Android con UTADEO es independiente de la API de persistencia propia.

## 2. Vista de desarrollo — organización del código

```mermaid
flowchart TB
    Repo[Repositorio Git]
    Repo --> Android[app/ Android Kotlin]
    Repo --> Backend[backend/ Spring Boot Java]
    Repo --> Infra[docker-compose.yml y observabilidad/]
    Repo --> Doc[docs/ y dossier-arquitectura/]
    Repo --> Exp[experimentos/ k6]
    Android --> Nav[AppNavHost y Routes]
    Android --> VMs[viewmodel/]
    Android --> Data[data/api + data/repository]
    Backend --> Ctrl[api/controller]
    Backend --> Security[api/auth y config]
    Backend --> SQL[db/migration + JdbcClient]
    Infra --> Grafana[Prometheus y Grafana]
```

Los límites de despliegue **no coinciden** con cada paquete de código: el backend sigue siendo una API Spring Boot unificada. Extraer tareas como microservicio fue una alternativa evaluada, no la decisión adoptada.

## 3. Vista de procesos — escenarios de ejecución

### Escenario A: autenticación y consulta de tareas

```mermaid
sequenceDiagram
    actor E as Estudiante
    participant A as Android
    participant API as Spring Boot
    participant S as AuthService
    participant DB as PostgreSQL
    E->>A: Inicia sesión
    A->>API: POST /api/auth/login
    API->>S: Validar credenciales
    S->>DB: Consultar usuario y hash BCrypt
    DB-->>S: Datos de usuario
    S->>DB: Guardar hash de token y vencimiento
    S-->>API: Token de sesión
    API-->>A: HTTP 200 + token
    E->>A: Consulta sus tareas
    A->>API: GET /api/tasks + Bearer
    API->>S: Validar token
    S->>DB: Buscar sesión vigente
    DB-->>S: ID del usuario
    API->>DB: SELECT tareas WHERE user_id = ID
    DB-->>API: Tareas del usuario
    API-->>A: HTTP 200 + lista
```

**Límite:** Android sigue utilizando `GET /api/tasks` sin paginación en `TaskRepository.getMyTasks()`. Aunque el backend expone `/api/tasks/page` y se probó su rendimiento, la adopción en Android está pendiente.

### Escenario B: recuperación manual observada

```mermaid
sequenceDiagram
    participant Op as Operador
    participant Docker as Docker Compose
    participant API as API Spring Boot
    participant Prom as Prometheus
    Op->>Docker: stop api
    Docker--xAPI: Interrumpe el contenedor
    Prom->>API: Scrape /actuator/prometheus
    API--xProm: Sin respuesta
    Note over Prom: up=0
    Op->>Docker: start api
    Docker->>API: Inicia proceso
    Prom->>API: Scrape
    API-->>Prom: Métricas
    Note over Prom: up=1
```

La evidencia procede del experimento registrado en `docs/modulo-7/05-riesgos-operativos-recuperacion.md`. No hay failover ni reinicio autónomo comprobado.

## 4. Vista física — despliegue observado en laboratorio

```mermaid
flowchart LR
    Device[Dispositivo Android] -->|HTTP REST puerto 8080| Host[Equipo de laboratorio Docker]
    subgraph Host[Equipo local con Docker Compose]
        API[Contenedor api Spring Boot :8080 y :8081]
        DB[(Contenedor db PostgreSQL :5432)]
        P[Prometheus :9090]
        G[Grafana :3000]
        V[(Volumen postgres_data)]
        API -->|JDBC| DB
        DB --- V
        P -->|Scrape api:8081| API
        G -->|Consulta| P
    end
```

El archivo Compose principal publica los puertos 8080 y 5432 en todas las interfaces del host; el Compose adicional publica Prometheus y Grafana solo en localhost. El puerto de gestión 8081 funciona en la red interna de Docker. Esta es la topología del laboratorio, **no** una infraestructura pública con TLS, réplicas o balanceo.

## +1. Escenarios y evidencia de calidad

| Escenario | Propiedad | Evidencia | Límite |
| --- | --- | --- | --- |
| Un usuario A consulta sus tareas | Aislamiento | `TaskPaginationFitnessTest` con PostgreSQL; verde–rojo–verde | Prueba focalizada, no auditoría integral |
| GET de tareas ante 30 VUs | Rendimiento | 19.303 GET completados; 311,59 GET/s; cero fallos; p95 HTTP agregado 119,01 ms | p95 incluye login, no representa exclusivamente GET |
| La API se detiene y se inicia | Recuperabilidad manual | `up=1` → `up=0` → `up=1`, health `UP` y ruta protegida responde 403 | Sin RTO medido, sin autorrecuperación |
| Peticiones concurrentes a PostgreSQL | Capacidad operativa | Máximo 10 activas, 14 pendientes y cero timeouts del pool | No identifica el cuello de botella |
| La app pide 50 tareas paginadas | Eficiencia por respuesta | Endpoint `/api/tasks/page`, ADR-007 y comparación M6 | Android todavía usa consulta original |

## Trazabilidad mínima

- `app/src/main/java/com/example/aprendeaprender/navigation/AppNavHost.kt`
- `app/src/main/java/com/example/aprendeaprender/data/api/ApiService.kt`
- `app/src/main/java/com/example/aprendeaprender/data/repository/TaskRepository.kt`
- `backend/src/main/java/com/example/aprendeaprender/api/controller/{AuthController,TaskController,SubjectController,ChallengeController}.java`
- `backend/src/main/java/com/example/aprendeaprender/api/auth/AuthService.java`
- `backend/src/main/java/com/example/aprendeaprender/api/config/SecurityConfig.java`
- `backend/src/main/resources/db/migration/`
- `docker-compose.yml`, `docker-compose.observabilidad.yml`, `observabilidad/`
- `docs/c4/`, `docs/modulo-7/04-resultados-observabilidad.md`, `docs/modulo-7/05-riesgos-operativos-recuperacion.md`

## Puntos pendientes

Los diagramas no prueban por sí mismos el funcionamiento. Falta comprobar visualmente la representación Mermaid en GitHub, añadir referencias a los screenshots originales de Grafana cuando queden versionados, validar el estado del pipeline de CI y preparar el cierre del Módulo 8. La recuperación de PostgreSQL desde respaldo y el despliegue público seguro siguen sin comprobarse.
