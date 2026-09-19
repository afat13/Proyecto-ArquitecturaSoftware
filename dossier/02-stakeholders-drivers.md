# 02 — Stakeholders, drivers y riesgos

## Stakeholders

| Stakeholder | Qué le importa |
| --- | --- |
| Estudiante | que sus tareas y materias estén disponibles y no se mezclen con las de otros usuarios |
| Equipo de desarrollo | poder cambiar backend o persistencia sin romper Android |
| Docente | poder comprobar lo que afirmamos mirando código y evidencia |
| UTADEO | es la fuente externa de parte de la información académica |
| Operación del backend | que API y PostgreSQL se puedan levantar y revisar fácilmente |

## Restricciones que ya tenemos

- Android ya existe y está hecho con Kotlin y Compose.
- El backend usa Spring Boot 3.3.4 y Java 21.
- La persistencia de este corte es PostgreSQL 16.
- Android no debe tener credenciales de PostgreSQL.
- Gemma sigue funcionando localmente.
- La integración con UTADEO depende de un sistema externo.
- El escenario actual se ejecuta en infraestructura local con Docker.

## Drivers principales

### Integridad de los datos

Cada usuario debe consultar sus propios datos. En tareas esto se ve en el filtro por `user_id` que hace el backend.

### Seguridad de autenticación

Las contraseñas se guardan con BCrypt. Para las sesiones se genera un token Bearer y en PostgreSQL se conserva el hash SHA-256 del token.

### Rendimiento

Elegimos `GET /api/tasks` porque es una operación fácil de seguir de extremo a extremo y porque el volumen de tareas puede crecer.

### Modificabilidad

La separación Android → API → PostgreSQL nos permite cambiar la persistencia sin meter SQL o credenciales dentro de Android.

### Reproducibilidad

Docker, Flyway, k6, los scripts y los resultados versionados hacen posible repetir el escenario.

## Riesgos que sí estamos considerando

| Riesgo | Qué hicimos |
| --- | --- |
| una consulta con muchas tareas puede crecer demasiado | medimos primero antes de proponer paginación |
| una consulta puede devolver datos de otro usuario si se pierde el filtro | mantenemos autenticación y `WHERE ... user_id` |
| UTADEO puede cambiar | la integración está separada en su propio servicio/repositorio |
| Gemma puede no estar disponible en el dispositivo | el modelo se gestiona localmente |
| alguien puede proponer infraestructura innecesaria | no agregamos cosas como Kubernetes sin una necesidad medida |

No estamos diciendo que PostgreSQL “sea lento” o “sea rápido” por intuición. El experimento mide una operación concreta; cualquier causa tendría que medirse aparte.
