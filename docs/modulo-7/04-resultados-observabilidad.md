# Resultados de observabilidad — Módulo 7

**Proyecto:** Aprende a Aprender  
**Fecha:** 6 de noviembre de 2026  
**Commit de referencia:** `02895d4`  
**Entorno:** PostgreSQL 16, API Spring Boot, Prometheus y Grafana en Docker Compose  
**Estado:** prueba de validación instrumentada; no corresponde a la re-medición final

## Objetivo y condiciones

Validamos la instrumentación del backend bajo tráfico autenticado. Utilizamos la semilla experimental existente, verificada mediante SQL: 5.000 usuarios de prueba y 1.000 tareas para `carga0001@aprende.local`. Se ejecutó `experimentos/consulta-tareas/carga-m6.js` con **30 usuarios virtuales y 60 segundos de duración**, desde el servicio k6 de Docker. El script realiza un login por VU durante `setup()` y consulta repetidamente `GET /api/tasks`.

El archivo local informado es `experimentos/observabilidad-m7/resultados/k6-30vus-60s-20261106-183116-02895d4.log`. **Este log todavía no está publicado en el repositorio**; los valores siguientes proceden de la salida compartida después de ejecutar la prueba.

## Resultado de k6

| Indicador | Observado |
| --- | ---: |
| Solicitudes HTTP totales, incluidos logins | 19.333 |
| Consultas GET de tareas | 19.303 |
| GET/s (consultas completas) | 311,59 |
| Checks satisfactorios | 38.606 / 38.606 (100 %) |
| HTTP fallidos | 0 / 19.333 |
| Latencia HTTP promedio, solicitudes agregadas | 63,54 ms |
| Latencia HTTP mediana, solicitudes agregadas | 57,63 ms |
| Latencia HTTP p95, solicitudes agregadas | 119,01 ms |
| Latencia HTTP máxima, solicitudes agregadas | 425,01 ms |
| Datos recibidos | 6,3 GB |

Todos los checks del script confirmaron HTTP 200 y la presencia de 1.000 tareas por consulta. El p95 anterior **incluye las solicitudes de inicio de sesión**; por tanto, no lo presentamos como p95 exclusivo de GET. K6 y el histograma del backend tampoco tienen el mismo punto de medición.

## Observaciones en Grafana

En las capturas de la ejecución se observaron:

- Objetivo `backend` disponible (serie `up = 1`).
- Respuestas HTTP 200 durante la carga, con tasas puntuales próximas a 168 req/s en la visualización. La consulta del panel aún aparecía con una ventana de dos minutos, por lo que esta tasa no equivale a la media de GET/s registrada por k6.
- Latencia p95 del servidor alrededor de 100–130 ms al final de la ventana mostrada; es una estimación gráfica, no un resumen consolidado.
- `hikaricp_connections_active` alcanzó 10 en un punto de la prueba; `hikaricp_connections_pending` presentó un pico visual cercano a 11. Se requiere comprobar con series y máximos exportados que las medidas correspondan a la misma ventana de prueba.
- Memoria heap de la JVM con crecimiento transitorio visible, con picos aproximados próximos a 240–256 MiB.
- CPU del proceso Java aproximadamente 28,1 % en un punto de la captura.

Estas observaciones **no establecen todavía cuál fue el recurso limitante**. Un pool lleno junto con peticiones en espera constituye una señal de presión, pero no prueba por sí mismo que ampliar las conexiones mejore el rendimiento. No se midió directamente la utilización del generador k6.

## Interpretación y límites

Esta prueba demuestra que la instrumentación expone y registra métricas mientras las solicitudes autenticadas son procesadas correctamente. No permite comparar de forma estricta contra las corridas históricas del M6 ni evaluar 50–200 VUs, porque cambiaron la instrumentación y posiblemente otras condiciones de ejecución.

Antes de decidir cambios sobre el pool de conexiones debemos consultar al menos `hikaricp_connections_max`, `hikaricp_connections_pending`, `hikaricp_connections_active` y `hikaricp_connections_timeout_total` (si existe), restringiendo el intervalo al de la prueba y contrastándolo con el p95 del backend y los resultados crudos de k6.

## Pendientes de cierre

1. Conservar en el repositorio la evidencia de la corrida sin secretos, junto con condiciones y commit de ejecución.
2. Verificar mediante consultas PromQL los máximos del pool y si hubo timeouts.
3. Ajustar el dashboard local a la última revisión de `main` y comparar la ventana de tasa con k6 sin equipararlas.
4. Diseñar una re-medición final con hipótesis y condiciones fijadas antes de ejecutar nuevas corridas.
