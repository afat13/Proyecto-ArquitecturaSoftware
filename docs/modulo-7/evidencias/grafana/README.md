# Capturas originales de Grafana — Módulo 7

**Proyecto:** Aprende a Aprender  
**Fecha de ejecución:** 6 de noviembre de 2026  
**Procedencia:** capturas PNG compartidas en la conversación durante la validación local; se conservan como archivos originales, sin recortes ni edición de contenido.  
**Alcance:** laboratorio Docker Compose con Spring Boot, PostgreSQL, Prometheus, Grafana y k6.

Se versionan **16 capturas de Grafana**. Los números del nombre identifican la captura recibida; para interpretar los experimentos prevalecen las marcas de tiempo y los valores reales de las series, junto con los resultados de k6. No se incluyen las capturas de Prometheus ni de GitHub Actions, porque esta carpeta se limita a Grafana.

## Panel inicial y validación de acceso

Las primeras capturas muestran el dashboard activo y después las solicitudes HTTP 403 sin sesión. Son evidencia de instrumentación, no de carga autenticada.

- [Grafana 2026-11-06 23:19:18](grafana-20261106-231918.png)
- [Grafana 2026-11-06 23:19:23](grafana-20261106-231923.png)
- [Grafana 2026-11-06 23:24:29](grafana-20261106-232429.png)
- [Grafana 2026-11-06 23:24:35](grafana-20261106-232435.png)

## Validación corta de carga (1 VU)

Primer tráfico autenticado; aparecen los HTTP 200, el p95 y el consumo JVM. El resumen cuantitativo procede de k6, no de estas capturas.

- [Grafana 2026-11-06 23:28:24](grafana-20261106-232824.png)
- [Grafana 2026-11-06 23:28:45](grafana-20261106-232845.png)

## Carga instrumentada (30 VUs)

Carga de 30 usuarios virtuales: GET exitosos, p95 del servidor, HikariCP, heap y CPU. La presión histórica del pool se confirmó después con PromQL: 10 conexiones activas, 14 pendientes, 0 timeouts en la ventana consultada.

- [Grafana 2026-11-06 23:32:39](grafana-20261106-233239.png)
- [Grafana 2026-11-06 23:32:59](grafana-20261106-233259.png)
- [Grafana 2026-11-06 23:33:14](grafana-20261106-233314.png)
- [Grafana 2026-11-06 23:33:33](grafana-20261106-233333.png)
- [Grafana 2026-11-06 23:42:02](grafana-20261106-234202.png)
- [Grafana 2026-11-06 23:42:10](grafana-20261106-234210.png)

## Caída y recuperación manual de API

La discontinuidad visible del indicador de disponibilidad corresponde a la interrupción manual de Spring Boot. Prometheus registró por separado `up=1 → up=0 → up=1`; las capturas finales muestran además el estado posterior. El contador acumulado puede reiniciarse con el proceso: no se deben sumar tramos discontinuos sin considerar el reinicio.

- [Grafana 2026-11-06 23:47:57](grafana-20261106-234757.png)
- [Grafana 2026-11-06 23:48:09](grafana-20261106-234809.png)
- [Grafana 2026-11-06 23:49:59](grafana-20261106-234959.png)
- [Grafana 2026-11-06 23:50:06](grafana-20261106-235006.png)

## Lectura de los resultados

- **Carga:** las series temporales muestran el comportamiento observado durante los ensayos; los datos agregados de k6 y el contexto experimental están en [resultados de observabilidad](../../04-resultados-observabilidad.md).
- **Pool HikariCP:** el máximo de 10 conexiones activas y 14 pendientes indica presión temporal, **no demuestra** por sí solo que el pool sea el cuello de botella principal.
- **Heap:** los picos y caídas son compatibles con actividad de GC o reinicio; las imágenes no permiten diagnosticar una fuga de memoria.
- **Disponibilidad:** la interrupción visible del objetivo se respaldó con las consultas Prometheus `up=0` y posterior `up=1`, además del healthcheck `UP` y respuesta HTTP 403 de ruta protegida. Fue una **recuperación manual**, sin failover automático ni RTO medido.
- **Diferencia de medición:** el p95 de Prometheus es del lado servidor; el p95 de k6 incluye el tiempo observado por el generador. No deben presentarse como equivalentes.

Más contexto en [recuperación y riesgos operativos](../../05-riesgos-operativos-recuperacion.md) y [dossier del tercer corte](../../../tercer-corte/README.md).
