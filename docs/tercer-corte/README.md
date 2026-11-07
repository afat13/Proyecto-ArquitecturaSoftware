# Dossier de cierre — tercer corte de Arquitectura de Software

**Proyecto:** Aprende a Aprender  
**Corte:** tercero  
**Corte documental:** 6 de noviembre de 2026  
**Referencia de validación CI:** commit `615a4fe`, ejecución [#214](https://github.com/afat13/Proyecto-ArquitecturaSoftware/actions/runs/42314951164)  
**Estado del dossier:** síntesis verificable; pendientes explícitos, no declaración de producto listo para producción.

## 1. Qué construimos y qué problema atendimos

Aprende a Aprender es una aplicación Android para organizar la actividad académica, gestionar materias y tareas, sincronizar información de UTADEO y apoyar retos de aprendizaje con Gemma ejecutado localmente. La persistencia principal funciona mediante una API REST Spring Boot y PostgreSQL 16; Android no accede directamente a la base de datos.

En este corte trabajamos sobre problemas de crecimiento, aislamiento de datos, decisiones de distribución, observabilidad y recuperabilidad. Evitamos separar servicios sin evidencia de beneficio. La alternativa documentada en ADR-007 mantiene una API Spring Boot unificada y propone optimizar primero las consultas de tareas.

## 2. Arquitectura implementada

- **Android:** Kotlin, Jetpack Compose, ViewModels, repositorios y Retrofit/OkHttp.
- **Backend:** Spring Boot, autenticación Bearer, autorización asociada a usuario, JdbcClient y migraciones Flyway.
- **Persistencia:** PostgreSQL 16, alojado en Docker en el laboratorio.
- **IA local:** Gemma en el dispositivo, no un microservicio remoto.
- **Observabilidad de laboratorio:** Micrometer, Prometheus y Grafana mediante Compose adicional.
- **Validación:** k6, tests del backend, tests/lint/build Android y GitHub Actions.

**Aclaración decisiva:** el endpoint paginado `GET /api/tasks/page?page=0&size=50` existe en el backend; el cliente Android aún consulta `GET /api/tasks` mediante `TaskRepository.getMyTasks()`. No se presenta como integración Android terminada.

Los diagramas C4 existentes están en [docs/c4](../c4/README.md). Las cinco perspectivas del modelo [4+1](../modulo-7/06-vistas-arquitectonicas-4mas1.md) complementan esos diagramas.

## 3. Evidencia de los módulos

| Tema | Qué se comprobó | Evidencia | Estado |
| --- | --- | --- | --- |
| Crecimiento M6 | 20 corridas con 30, 50, 100, 150 y 200 VUs; p95 agregado aumentó de 144,66 a 1.287,58 ms entre escalones | [Análisis M6](../../experimentos/crecimiento-m6/01-analisis-20261023.md), [README M6](../modulo-6/README.md) | Realizado; cuello de botella no identificado |
| Diseño de distribución | Alternativas y costos relativos; decisión de mantener API unificada y optimizar consultas | [ADR-007](../adr/ADR-007-distribucion-resiliencia-modulo6.md), [distribución y costos](../modulo-6/distribucion-costos.md) | Documentado |
| Comparación de paginación | Mediana p95 de GET por petición: 86,68 ms (1.000 tareas) frente a 15,92 ms (50 tareas); no equivalen a transferir la misma cantidad de tareas | [Comparación](../../experimentos/crecimiento-m6/03-resultados-paginacion.md) | Medido, sin adopción completa en Android |
| Fitness function | Secuencia verde–rojo–verde: al permitir tamaño 1.000, la prueba rechazó correctamente la violación del máximo de 100; restaurado el código, 4/4 pruebas verdes | [Evidencia](../modulo-6/fitness-function-m6-evidencia.md), [JUnit rojo](../modulo-7/evidencias/fitness-rojo-junit.xml), [JUnit verde](../modulo-7/evidencias/fitness-verde-final-junit.xml) | Verificada localmente; job backend aprobado en CI |
| Amenazas | Matriz STRIDE; endpoints protegidos responden 403 sin sesión; riesgos de HTTP y puerto PostgreSQL expuesto | [Modelado de amenazas](../modulo-7/01-modelado-amenazas.md) | Analizado; mitigaciones parciales |
| Observabilidad | Prometheus scrape `api:8081`, dashboard Grafana, series de HTTP, HikariCP, heap, CPU | [Configuración](../modulo-7/03-observabilidad.md) | Comprobada en laboratorio |
| Carga instrumentada M7 | 30 VUs, 60 s; 19.303 GET exitosos y 38.606/38.606 checks; 0 fallos HTTP; 311,59 GET/s; p95 agregado HTTP 119,01 ms | [Informe](../modulo-7/04-resultados-observabilidad.md), [log k6](../../experimentos/observabilidad-m7/resultados/k6-30vus-60s-20261106-183116-02895d4.log) | Comprobada |
| Pool de conexiones | 10 conexiones activas máximas, 14 pendientes máximas, 0 timeouts en ventana examinada | [Resultados M7](../modulo-7/04-resultados-observabilidad.md) | Presión observada; causalidad no probada |
| Recuperación | Parada manual de API: `up=1 → up=0 → up=1`; health vuelve a `UP`; PostgreSQL sigue activo | [Riesgos y recuperación](../modulo-7/05-riesgos-operativos-recuperacion.md) | Comprobada manualmente; no failover ni RTO medido |
| Integración continua | Ejecución #214: job Backend y PostgreSQL aprobado; job Android aprobado, incluyendo pruebas, lint y APK | [GitHub Actions #214](https://github.com/afat13/Proyecto-ArquitecturaSoftware/actions/runs/42314951164) | **Verde** |

**Cuidado con los percentiles:** los 119,01 ms de M7 corresponden a la métrica HTTP agregada reportada por k6 (incluye logins); no son un p95 exclusivo de GET ni equivalen al p95 observado desde Prometheus.

## 4. Qué decidimos y por qué

Mantener Spring Boot y PostgreSQL como arquitectura actual evita añadir operaciones, despliegues y costos de microservicios sin una causa experimental demostrada. La paginación se implementó y probó en el backend para reducir trabajo por petición, pero aún falta adaptar el flujo Android sin perder tareas filtradas por vencimiento, fecha o materia. El pico del pool HikariCP sirve para proponer un análisis, no para justificar aumentar conexiones de forma automática.

La observabilidad se implementó porque las mediciones originales de k6 no explicaban por sí solas el comportamiento de la JVM y la base de datos. La simulación de caída mostró que Prometheus detecta la interrupción y que el servicio puede recuperarse manualmente.

## 5. Riesgos y pendientes para una defensa precisa

**Prioridad alta de arquitectura/seguridad:**

- PostgreSQL aún se publica en `0.0.0.0:5432` en el laboratorio.
- La comunicación móvil-API de desarrollo usa HTTP; producción requeriría HTTPS y tratamiento adecuado de secretos.
- No se probó restaurar un respaldo PostgreSQL; volumen persistente no equivale a backup.
- La integración de paginación en Android está pendiente.
- Los JSON antiguos del experimento M6 llegaron a contener tokens en commits históricos; la depuración de archivos actuales no limpia automáticamente el historial. La revocación/limpieza no está verificada.

**Pendientes documentales/experimentales:**

- Publicar los PNG originales de Grafana junto con fecha y consulta/intervalo de cada panel. Las capturas compartidas respaldan el análisis, pero aún no están versionadas.
- Actualizar las notas antiguas del M6 que todavía dicen que la fitness function y el CI están pendientes: el presente dossier registra el estado posterior.
- Spike 1 de la frontera Tareas–Materias del M5: preregistro, ejecución, datos crudos y decisión siguen pendientes; no simular resultados.
- Recuperación automática, failover, RTO/RPO y comparación controlada de cambio del pool no demostrados.
- Comprobar el render de diagramas en GitHub y conservar una versión visible para la exposición.

## 6. Orden propuesto para sustentar

1. **Contexto y arquitectura**: qué hace Android y qué responsabilidades quedan en la API y PostgreSQL; C4 y 4+1.
2. **Problema de calidad**: crecimiento de latencia con concurrencia; mediciones iniciales y límites.
3. **Decisión**: comparar alternativas y defender el ADR-007; optimización paginada sin microservicios.
4. **Verificación**: tests, verde–rojo–verde, pipeline CI #214.
5. **Operación**: Prometheus/Grafana, carga 30 VUs, pool Hikari y experimento de recuperación.
6. **Cierre responsable**: presentar resultados observados, riesgos abiertos y siguiente iteración (paginación Android + seguridad/backup).

Una demostración válida puede apoyarse en el pipeline verde, el dashboard aprovisionado, las tablas del log de k6 y la evidencia JUnit. No hace falta repetir la prueba de 60 segundos durante la exposición si ya se conservan sus resultados.

## 7. Fuentes principales para navegar el repositorio

- [Módulo 6](../modulo-6/README.md) y [trazabilidad M6](../modulo-6/trazabilidad.md).
- [ADR-007](../adr/ADR-007-distribucion-resiliencia-modulo6.md).
- [Módulo 7: amenazas](../modulo-7/01-modelado-amenazas.md), [despliegue](../modulo-7/02-diseno-despliegue.md), [observabilidad](../modulo-7/03-observabilidad.md), [resultados](../modulo-7/04-resultados-observabilidad.md), [recuperación](../modulo-7/05-riesgos-operativos-recuperacion.md) y [4+1](../modulo-7/06-vistas-arquitectonicas-4mas1.md).
- [Código Android TaskRepository](../../app/src/main/java/com/example/aprendeaprender/data/repository/TaskRepository.kt).
- [GitHub Actions #214, verde](https://github.com/afat13/Proyecto-ArquitecturaSoftware/actions/runs/42314951164).

**Criterio de cierre:** decir «hecho» solo cuando exista evidencia; «documentado» cuando haya una decisión o propuesta; «pendiente» cuando no se haya ejecutado o validado.
