# Riesgos operativos y recuperación ante fallos — Módulo 7

**Proyecto:** Aprende a Aprender  
**Fecha:** 6 de noviembre de 2026  
**Estado:** simulación local de caída y recuperación manual de API comprobada; recuperación de datos y autorrecuperación pendientes  
**Alcance:** laboratorio Docker Compose; no representa disponibilidad de producción.

## 1. Situación de partida

Actualmente la solución se compone de una aplicación Android, API Spring Boot y PostgreSQL 16. El archivo `docker-compose.yml` levanta `db` y `api`, mientras que `docker-compose.observabilidad.yml` incorpora Prometheus y Grafana. El backend usa Flyway, autenticación Bearer y un pool HikariCP configurado con 10 conexiones máximas por defecto.

Durante una prueba de 30 usuarios virtuales y 60 segundos observamos 19.303 consultas GET de tareas completadas, con checks correctos y sin respuestas HTTP fallidas. Las series históricas del intervalo registraron 10 conexiones activas, hasta 14 pendientes y cero timeouts del pool. Eso sugiere presión temporal; no identifica de manera definitiva el recurso que limita el rendimiento.

Las pruebas previas de acceso sin sesión devolvieron HTTP 403 en `/api/tasks`, `/api/subjects` y `/api/profile`. El endpoint de métricas se utiliza en el puerto interno 8081 en el modo de observabilidad; no debe publicarse al exterior.

## 2. Riesgos principales

| Riesgo | Escenario | Impacto | Control actual | Mejora / verificación |
| --- | --- | --- | --- | --- |
| O1 — Caída de API | El contenedor Spring Boot se detiene | No hay operaciones del cliente mientras dura la caída | Healthcheck; Docker Compose permite reiniciar el servicio | Ejecutar simulación y medir recuperación; no se ha validado autorrecuperación |
| O2 — Caída de PostgreSQL | La base de datos deja de responder | Consultas y sesiones fallan o se degradan | Volumen de datos; condición `service_healthy` para arranque | Prueba aislada de recuperación y respaldo; no ejecutar en la base actual sin copia |
| O3 — Exposición de BD | Publicación `0.0.0.0:5432` | Mayor superficie de acceso desde interfaces del host | Credenciales del entorno local | Limitar puerto a localhost o eliminar publicación si solo accede Docker; validar aplicaciones dependientes |
| O4 — HTTP sin cifrar | Tránsito API móvil-servidor por HTTP | Riesgo de exposición de tokens y respuestas en redes no confiables | Token Bearer; no hay TLS en despliegue local | Usar HTTPS con proxy/certificados y deshabilitar tráfico claro en distribución |
| O5 — Pool bajo presión | Se ocupan las 10 conexiones y hay esperas | Mayor latencia de solicitudes | HikariCP; consultas parametrizadas | Investigar duración de consultas, adquisición y CPU; comparación controlada antes de cambiar tamaño |
| O6 — Disco / datos | Falla el volumen PostgreSQL o se borra por error | Pérdida de datos | Volumen Docker persistente | Copias `pg_dump`, restauración comprobada, política de retención |
| O7 — Observabilidad no persistente | Prometheus se recrea sin volumen de datos | Se pierde historial de series | Configuración de scrape y dashboards en Git | Definir retención y almacenamiento persistente; guardar exportaciones de experimentos |
| O8 — Dependencia única | Una sola instancia API y una sola BD | Punto único de fallo | Arranque reproducible con Compose | Aceptar limitación del prototipo; valorar redundancia solo cuando haya exigencias y evidencia |

La priorización provisional es **alta** para O2, O3, O4 y O6; **media** para O1, O5, O7 y O8. La calificación es cualitativa y debe revisarse según el entorno real de despliegue, no se deriva de una matriz probabilística formal.

## 3. Escenario de recuperación controlada — API

**Hipótesis:** al detener manualmente solo el contenedor de API, Prometheus debe detectar pérdida temporal de su objetivo. Tras volver a iniciarlo, el healthcheck y la serie `up` deben recuperar su estado normal sin reinicializar PostgreSQL.

**Precondiciones:** usar exclusivamente el laboratorio local, sin usuarios reales ni una carga k6 activa; contenedores `db`, `api`, `prometheus`, `grafana` iniciados; respaldo de evidencia y consultas de control. Mantener las dos configuraciones Compose en todos los comandos.

**Secuencia:**

1. Guardar hora y SHA local, comprobar `docker compose -f docker-compose.yml -f docker-compose.observabilidad.yml ps` y `up{job="backend"}`.
2. Detener **solo** la API: `docker compose -f docker-compose.yml -f docker-compose.observabilidad.yml stop api`.
3. Observar durante varios intervalos de scrape el estado del objetivo en Prometheus, y registrar `up{job="backend"}` (debería pasar a 0 una vez que falle el scrape). Grafana y Prometheus siguen ejecutándose.
4. Levantar **solo** la API: `docker compose -f docker-compose.yml -f docker-compose.observabilidad.yml start api`.
5. Verificar el estado `healthy` del servicio API, la recuperación `up=1` y una solicitud de negocio de lectura autenticada o, como comprobación mínima, `http://localhost:8080/api/tasks` sin credenciales respondiendo 403. El HTTP 403 demuestra que vuelve a responder la protección del endpoint, **no** que todas las funciones estén correctas.
6. Conservar capturas, hora de caída, hora de recuperación y diferencias observadas. No declarar RTO/RPO formal sin medición, definición de criterio y respaldo.

**No ejecutar:** `docker compose down -v`, eliminación de volúmenes ni detención de `db` para este escenario.

## 4. Criterios de aceptación y evidencia esperada

- Antes de la interrupción, API saludable y objetivo Prometheus `up=1`.
- Durante la interrupción, el objetivo de Prometheus cambia a `up=0` (tras un scrape fallido).
- Tras `start api`, API saludable y objetivo de Prometheus retorna a `up=1`.
- El contenedor `db` permanece saludable. No deben perderse las 5.000 cuentas de la semilla experimental.
- Guardar terminal y métricas con marcas de tiempo. Un restart manual exitoso **no equivale a failover ni autorrecuperación automática**.

## 5. Ejecución real del escenario de API — 6 de noviembre de 2026

El experimento se realizó con el servicio `api` y la observabilidad desplegados en Docker Compose. La primera comprobación se registró a las **18:45:46 (America/Bogota)** con PostgreSQL saludable y `up{job="backend"}=1`. No se ejecutaba carga k6 durante la interrupción.

| Etapa | Acción y resultado observado |
| --- | --- |
| Estado inicial | `api` y `db` saludables; Prometheus registró `backend: 1` |
| Caída manual | `docker compose ... stop api` detuvo solo la API; Prometheus registró `backend: 0` |
| Inicio manual | `docker compose ... start api` inició la API; el primer `ps` mostró `health: starting` |
| Recuperación comprobada | `curl http://localhost:8081/actuator/health` devolvió `{"status":"UP","groups":["liveness","readiness"]}` |
| Captura de disponibilidad | Una nueva consulta a Prometheus devolvió `backend: 1` y las capturas de Grafana muestran la interrupción entre intervalos disponibles |
| Control de acceso | `GET http://localhost:8080/api/tasks` sin autenticación devolvió HTTP 403, coherente con la configuración de seguridad |
| Verificación final | Hora de la comprobación: **18:47:53 (America/Bogota)**; PostgreSQL no fue detenido |

**Interpretación:** en el laboratorio se pudo recuperar manualmente la API y Prometheus registró correctamente pérdida y regreso del objetivo. Las verificaciones no constituyen una prueba de autorrecuperación, alta disponibilidad ni tolerancia a fallos simultáneos. La duración desde la caída hasta el restablecimiento de la disponibilidad no se midió con marcas de tiempo de cada transición; **no se declara RTO medido**. Tampoco se comprobó una restauración de respaldo ni un RPO.

Las [capturas originales de Grafana](evidencias/grafana/README.md) quedaron versionadas en el repositorio; incluyen la discontinuidad de disponibilidad durante la interrupción. El resultado de una ruta protegida que responde 403 solo comprueba que la API vuelve a aplicar autenticación, no que todas las operaciones de negocio estén correctas.

## 6. Recuperación de datos (diseño, aún no validado)


Se propone realizar `pg_dump` de la base en un entorno aislado, revisar el archivo resultante y ensayar `pg_restore` sobre **otra base de pruebas**, comprobando tablas, migraciones Flyway y conteos. No se ha ejecutado todavía ese procedimiento. El volumen Docker **no constituye una copia de seguridad**. Nunca ejecutar una restauración sobre `aprende_aprender` sin confirmar consecuencias.

## 7. Próximas decisiones

No aumentar todavía el pool por el solo hecho de ver pendientes. Priorizar medir consultas lentas, uso del generador y percentiles separados por operación. Mantener la decisión del ADR-007 sobre optimizaciones progresivas. Antes de exponer el sistema públicamente deberán definirse TLS, secretos, respaldo probado y restricción de la red de base de datos.

**Referencias internas:** [diseño de despliegue](02-diseno-despliegue.md), [observabilidad](03-observabilidad.md), [resultados de observabilidad](04-resultados-observabilidad.md) y [ADR-007](../adr/ADR-007-distribucion-resiliencia-modulo6.md).
