# Observabilidad — laboratorio local M7

**Proyecto:** Aprende a Aprender  
**Estado:** configuración y series validadas en laboratorio; criterios de alerta propuestos, no automatizados  
**Fecha:** 6 de noviembre de 2026

## Propósito

En el módulo 6 registramos tiempos de respuesta y carga con k6, pero no teníamos una señal continua de qué ocurría dentro del backend. En esta etapa añadimos métricas para contrastar las pruebas HTTP con señales de la JVM y de las conexiones a PostgreSQL. No asumimos que una métrica por sí sola demuestra la causa de una degradación.

## Arquitectura propuesta y alcance

- **Spring Boot / Micrometer:** expone métricas en el puerto de administración interno 8081 solo cuando activamos el archivo Compose de observabilidad.
- **Prometheus:** consulta `api:8081/actuator/prometheus` cada cinco segundos. Su UI se publica solo en `127.0.0.1:9090`.
- **Grafana:** consulta a Prometheus usando `http://prometheus:9090`. Su UI se publica solo en `127.0.0.1:3000`.
- **k6:** permanece como generador de carga y conserva sus resultados históricos aparte; todavía no está conectado por remote write.

El puerto 8081 no se publica al host, pero los contenedores que comparten la red Compose pueden acceder a él. Este esquema es de laboratorio, no un diseño de seguridad suficiente para producción.

En este modo `/actuator/health` se traslada al puerto interno 8081. La API de negocio continúa escuchando en el 8080.

## Comprobaciones realizadas y trazabilidad

La API, PostgreSQL, Prometheus y Grafana se iniciaron en Docker; Prometheus mostró `backend 1/1 UP` y el dashboard registró tráfico autenticado. La corrida de 30 VUs y 60 s se resume en [04-resultados-observabilidad](04-resultados-observabilidad.md), con [log k6](../../experimentos/observabilidad-m7/resultados/k6-30vus-60s-20261106-183116-02895d4.log) y [16 capturas](evidencias/grafana/README.md). La interrupción manual se observó mediante `up=1 → 0 → 1` y se documenta en [05-riesgos-operativos-recuperacion](05-riesgos-operativos-recuperacion.md).

## Lista de verificaciones usada en laboratorio

1. Confirmar que `docker compose ... config` es válido.
2. Levantar los servicios y comprobar que API, PostgreSQL, Prometheus y Grafana se mantienen en ejecución.
3. Revisar en `http://localhost:9090/targets` que el objetivo `backend` esté en estado UP.
4. Ejecutar consultas en Prometheus: `up{job="backend"}`, `jvm_memory_used_bytes`, `hikaricp_connections_active` y `http_server_requests_seconds_count`.
5. Generar tráfico autorizado para comprobar series HTTP; no asumir que una serie existe antes de recibir peticiones.
6. Confirmar que Grafana trae por aprovisionamiento la fuente de datos Prometheus.

## Paneles propuestos

| Panel | Tipo | Métrica / consulta inicial | Unidad |
| --- | --- | --- | --- |
| Disponibilidad del scraper | State timeline | `up{job="backend"}` | estado 0/1 |
| Solicitudes por segundo | Time series | `sum(rate(http_server_requests_seconds_count[1m]))` | req/s |
| Códigos HTTP | Time series | `sum by (status)(rate(http_server_requests_seconds_count[1m]))` | req/s |
| Latencia p95 del backend | Time series | `1000 * histogram_quantile(0.95, sum by (le)(rate(http_server_requests_seconds_bucket[1m])))` | ms |
| Conexiones activas | Time series | `hikaricp_connections_active` | conexiones |
| Conexiones pendientes | Time series | `hikaricp_connections_pending` | conexiones |
| Memoria heap utilizada | Time series | `sum(jvm_memory_used_bytes{area="heap"}) / 1024 / 1024` | MiB |

Las consultas son puntos de partida. Antes de guardarlas como paneles definitivos debemos validar nombres, etiquetas y series reales en Prometheus. El p95 del servidor no es equivalente al p95 completo de k6.

## Riesgos y límites

La publicación de PostgreSQL en `0.0.0.0:5432` procede del Compose principal y sigue pendiente de restringirse. El dashboard no corrige ese riesgo.

No exponer el puerto de administración 8081 al host ni copiar credenciales de Grafana al repositorio. La contraseña debe quedar únicamente en el archivo `.env` local.

Los resultados históricos del módulo 6 están en JSON y no aparecen automáticamente en Prometheus. La re-medición final tendrá que conservar condiciones, commit y evidencia original.

## Plan operativo: escenario, señal, propuesta de alerta y reacción

No se configuraron aún reglas automáticas ni envío de notificaciones. Los valores siguientes son **criterios de ensayo propuestos** a revisar por el equipo; no son SLO ni límites aprobados para producción. La tasa de recolección actual es de 5 s, y deben considerarse gaps y reinicios al interpretar series.

| Escenario y pregunta | Señal (consulta ilustrativa) | Unidad y condición propuesta | Acción operativa | Límite |
| --- | --- | --- | --- | --- |
| API inaccesible: ¿se perdió el scrape? | `up{job="backend"}` | 0/1; proponer alerta si `up == 0` durante 30 s | Revisar `docker compose ps`, logs y health; recuperar manualmente solo API si corresponde | `up=1` no demuestra experiencia de usuario |
| Errores de negocio: ¿subieron los 5xx? | `sum(rate(http_server_requests_seconds_count{job="backend",status=~"5.."}[2m]))` | req/s; proponer investigar si tasa > 0 durante 2 min en entorno de validación | Revisar trazas/logs, dependencia DB, disponibilidad y versión desplegada | HTTP 403 no equivale a fallo de servidor; tráfico bajo distorsiona porcentajes |
| Latencia GET de tareas: ¿se degradó? | `1000 * histogram_quantile(0.95, sum by (le)(rate(http_server_requests_seconds_bucket{job="backend",uri="/api/tasks",method="GET",status="200"}[2m])))` | ms; criterio de revisión ligado a línea base y protocolo, **no fijado todavía** | Ver tasa, códigos, pool, CPU; contrastar con p95 de k6 de la misma operación y ventana | Histograma del servidor no equivale a k6 ni garantiza p95 con muestras escasas |
| Pool de BD: ¿hay espera? | `hikaricp_connections_pending{job="backend"}` y `hikaricp_connections_active{job="backend"} / hikaricp_connections_max{job="backend"}` | conexiones y fracción; proponer investigar pendientes > 0 durante 30 s | Revisar duración de transacciones, consultas y timeouts; no subir conexiones automáticamente | Pico de pendientes no prueba saturación de PostgreSQL |
| ¿Agotamiento de conexiones con errores? | `increase(hikaricp_connections_timeout_total{job="backend"}[2m])` | eventos; proponer investigar cualquier incremento > 0 | Revisar logs y disponibilidad DB; correlacionar con 5xx | El contador puede reiniciarse con la API |
| ¿Consumo de heap anómalo? | `sum(jvm_memory_used_bytes{job="backend",area="heap"})` | bytes; sin umbral hasta comparar con límite máximo JVM y periodos tras GC | Revisar GC, heap máximo, carga y retención de memoria | Aumento de heap no demuestra fuga |

**Procedimiento de investigación propuesto:** registrar hora y SHA, capturar la consulta PromQL y rango, observar si el target está UP, comprobar salud/logs Docker, correlacionar con carga y cambios recientes, registrar intervención y resultado. No publicar contraseñas, tokens ni archivos `.env`. Para un incidente real hacen falta responsables asignados y canales de alerta aprobados.

**Responsable pendiente:** el equipo debe designar roles para vigilar métricas, autorizar cambios y recuperar datos; no se asignaron nombres sin aprobación.
