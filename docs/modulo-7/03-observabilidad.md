# Observabilidad — laboratorio local M7

**Proyecto:** Aprende a Aprender  
**Estado:** configuración preparada; validación pendiente de ejecución local  
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

## Pruebas pendientes

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
