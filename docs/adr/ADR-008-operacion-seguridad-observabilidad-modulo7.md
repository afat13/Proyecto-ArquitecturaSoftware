# ADR-008 — Operación observable y controles prioritarios del laboratorio

**Proyecto:** Aprende a Aprender  
**Módulo:** 7 (ADR 5 de la asignatura)  
**Fecha de elaboración:** 6 de noviembre de 2026  
**Estado:** **PROPUESTO — pendiente de ratificación del equipo**  
**Ámbito:** laboratorio Docker Compose; no constituye diseño de producción.

## Contexto y evidencia

La solución funciona actualmente con una API Spring Boot unificada y PostgreSQL 16, consumida por Android. El Módulo 6 mostró un crecimiento de latencia al aumentar la concurrencia; la causa principal de esa degradación no quedó demostrada. El Módulo 7 añadió Micrometer, Prometheus y Grafana a un despliegue opcional de laboratorio para poder observar el backend sin dividirlo en microservicios.

En una prueba con 30 VUs durante 60 segundos, k6 registró 19.303 consultas GET de tareas correctas, cero solicitudes fallidas y p95 HTTP agregado de 119,01 ms, **que incluye los inicios de sesión**. Consultas retrospectivas de Prometheus registraron 10 conexiones HikariCP activas (límite 10), 14 pendientes y cero timeouts en la ventana analizada. La saturación temporal del pool no demuestra por sí sola cuál es el recurso limitante.

La parada manual de la API se reflejó en Prometheus mediante `up=1 → up=0 → up=1`; después del inicio se recuperó el healthcheck `UP`. PostgreSQL permaneció activo. Este ensayo no prueba recuperación automática ni RTO.

La revisión STRIDE identificó, entre otros riesgos, tráfico HTTP sin TLS en el laboratorio, PostgreSQL publicado en `0.0.0.0:5432`, secretos locales y ausencia de una restauración de respaldo comprobada.

## Alternativas consideradas

| Alternativa | Ventajas esperadas | Costos y sacrificios | Evidencia / limitación |
| --- | --- | --- | --- |
| A. Conservar Compose para laboratorio, instrumentar y reducir exposición progresivamente | Reutiliza el despliegue existente, facilita pruebas y trazabilidad, evita infraestructura adicional | Depende de una sola API/BD; operación manual; requiere configurar respaldos, puertos y TLS antes de un despliegue externo | Observabilidad y recuperación manual ya probadas |
| B. Publicar una VM con API y PostgreSQL, TLS, respaldos y monitoreo | Facilita acceso remoto con controles operativos explícitos | Administración de máquina, certificados, parches, copias y costo recurrente | Alternativa propuesta; no probada ni cotizada |
| C. Réplicas de API más BD administrada y observabilidad | Puede ofrecer opciones superiores de disponibilidad y operación | Mayor costo, complejidad, despliegues, configuración de red y nuevas fuentes de fallo | No existe evidencia de necesidad ni ensayo de esta topología |

Los costos se comparan **cualitativamente**. No hay presupuestos ni mediciones comparables entre B y C, por lo que no se atribuyen ahorros monetarios.

## Propuesta de decisión

**Proponemos adoptar A para el laboratorio y las evidencias del curso**, conservando Spring Boot y PostgreSQL, con las siguientes prioridades: (1) mantener Prometheus/Grafana accesibles solo localmente, (2) restringir la publicación del puerto PostgreSQL después de revisar las pruebas que usan `host.docker.internal`, (3) definir respaldo y ensayar restauración en otra base, (4) documentar un mecanismo de HTTPS para cualquier despliegue externo y (5) registrar hipótesis y métricas antes de cambiar el tamaño del pool.

La alternativa A es coherente con el ADR-007, pero **esta propuesta no está aprobada por el equipo todavía**. No se declara que ya se hayan aplicado los controles pendientes.

## Consecuencias

**Positivas esperadas:** menos cambios estructurales, trazabilidad de mediciones, menor esfuerzo de despliegue en el laboratorio y posibilidad de investigar degradaciones con datos internos.

**Negativas y riesgos residuales:** API y BD siguen siendo puntos únicos de fallo; métricas sin retención durable no sustituyen registros de evidencia; una detención necesita intervención manual; el tráfico local HTTP y el puerto actual de PostgreSQL requieren tratamiento antes de uso externo.

## Reversibilidad y supuestos

Esta decisión puede revisarse cambiando la topología Compose y migrando a una VM o infraestructura administrada. La reversión implica redes, secretos, pipeline, respaldos, costos y posiblemente cambios en las direcciones usadas por Android. No debe asumirse migración automática de datos.

Se supone un entorno académico/local, sin SLA de producción, sin usuarios reales y sin necesidad demostrada de replicación. La semilla de carga no representa necesariamente la distribución de tareas de usuarios reales.

## Condiciones para reabrir el ADR

- Se exige disponibilidad o recuperación automática con RTO/RPO definidos.
- El acceso externo requiere TLS, control de secretos y conexión segura a PostgreSQL.
- Una prueba reproducible evidencia degradación relevante que no se resuelve con optimizaciones justificadas.
- Cambian los costos, condiciones de operación o restricciones del curso.

## Evidencia y verificación

- [Amenazas STRIDE](../modulo-7/01-modelado-amenazas.md).
- [Despliegue actual y costos relativos](../modulo-7/02-diseno-despliegue.md).
- [Plan de observabilidad](../modulo-7/03-observabilidad.md).
- [Resultados de carga y HikariCP](../modulo-7/04-resultados-observabilidad.md).
- [Riesgos y recuperación manual](../modulo-7/05-riesgos-operativos-recuperacion.md).
- [Capturas originales de Grafana](../modulo-7/evidencias/grafana/README.md).
- [ADR-007 — Distribución y resiliencia](ADR-007-distribucion-resiliencia-modulo6.md).
- [CI #217 aprobado](https://github.com/afat13/Proyecto-ArquitecturaSoftware/actions/runs/42318012828).

## Ratificación pendiente

El equipo debe registrar su veredicto y autoría real, indicando si acepta, modifica o rechaza la alternativa A. Hasta entonces el estado de este ADR permanece **PROPUESTO**, aunque el laboratorio use actualmente una topología coherente con ella.
