# ADR-008 — Observabilidad y recuperación operativa del laboratorio

**Proyecto:** Aprende a Aprender  
**Módulo:** 7 — Seguridad, despliegue y operación  
**Correspondencia académica:** ADR 5 del sílabo (numeración interna: ADR-008)  
**Fecha:** 6 de noviembre de 2026  
**Estado:** **Propuesta para ratificación del equipo**. La implementación técnica existe y se probó, pero este documento no constituye un acta de aprobación.

## Contexto

La arquitectura utiliza Android, Spring Boot y PostgreSQL 16 en Docker Compose. Las mediciones originales de crecimiento (30–200 VUs) mostraron degradación de latencia, sin identificar de forma concluyente el recurso que la causaba. Los contenedores de API y base de datos tienen comprobaciones de salud, pero esto no basta para observar cambios de rendimiento ni analizar esperas internas.

El 6 de noviembre instrumentamos Micrometer con Prometheus y Grafana, usando un archivo Compose adicional. En una prueba de 30 VUs por 60 segundos se completaron 19.303 consultas de tareas, sin fallos HTTP; el pool HikariCP llegó a 10 conexiones activas y registró hasta 14 solicitudes pendientes, con cero timeouts en la ventana examinada. En un ejercicio posterior, detener manualmente la API provocó `up=0` en Prometheus; al reiniciarla, volvió `up=1` y el healthcheck respondió `UP`.

No se ha probado autorrecuperación, restauración desde backup ni un despliegue público con TLS.

## Alternativas examinadas

| Alternativa | Ventaja | Costo y sacrificio |
| --- | --- | --- |
| A — Solo Actuator y logs locales | Cambio mínimo, sin nuevos servicios | No retiene series temporales comparables ni proporciona paneles continuos |
| B — Prometheus y Grafana con Compose adicional (implementada) | Métricas de HTTP/JVM/HikariCP, historial y paneles reproducibles; compatible con la API única | Más contenedores, consumo de recursos, mantenimiento y protección de endpoints; Prometheus no tiene almacenamiento persistente garantizado |
| C — Plataforma externa administrada | Puede facilitar alertas, retención y acceso remoto | Dependencia y posibles costos externos, gestión de secretos y exportación de datos; no necesaria para el laboratorio actual |

Los costos son relativos y cualitativos. No se han cotizado proveedores.

## Decisión propuesta para ratificación

**Mantener la alternativa B exclusivamente en el laboratorio**, sin obligar a que los servicios de observabilidad formen parte del despliegue mínimo de la aplicación. Esta propuesta documenta la configuración implementada; **falta que el equipo confirme explícitamente la decisión y el riesgo aceptado**.

La API de negocio permanece en 8080. La administración se expone en 8081 dentro de la red Docker cuando se aplica el Compose adicional. Prometheus recopila `/actuator/prometheus` cada 5 segundos y Grafana visualiza las series. Las interfaces de Prometheus y Grafana se publican únicamente en localhost del host.

## Consecuencias

**Positivas:** se pueden investigar latencia HTTP por ruta, CPU, memoria, conexiones activas/pendientes y pérdida de disponibilidad; las capturas y consultas aportan evidencia a las pruebas de k6.

**Negativas:** más complejidad operativa; las consultas PromQL pueden suavizar picos según la ventana; métricas y pantallas no demuestran causalidad; los contadores pueden reiniciarse cuando reinicia la API. En el estado actual, las series históricas pueden perderse al recrear Prometheus porque no se ha configurado un volumen persistente de datos.

## Reversibilidad

Retirar `docker-compose.observabilidad.yml` de la ejecución devuelve el laboratorio a los servicios principales; la API de negocio no requiere Grafana para responder. Retirar Micrometer y la configuración de seguridad de administración necesita cambio de código, ejecución de tests y revisión en CI. La reversión debe preservar los logs y resultados experimentales.

## Supuestos

La prueba ocurre en un equipo controlado, sin usuarios de producción. Los puertos publicados y credenciales locales no son una política de exposición pública. La captura cada 5 segundos no garantiza observar todos los picos. K6 y Prometheus miden desde puntos distintos.

## Condiciones para reconsiderar

Revisar la elección si el sistema necesita operar fuera del laboratorio, almacenar métricas más allá del ciclo de vida de contenedores, generar alertas con responsables/umbrales, manejar datos sensibles o cumplir objetivos medibles de recuperación. En esos casos se requieren controles de red, HTTPS, secretos, retención y evaluación de alternativas.

## Evidencia que respalda el análisis

- [Configuración de observabilidad](../modulo-7/03-observabilidad.md)
- [Resultados de la carga](../modulo-7/04-resultados-observabilidad.md)
- [Prueba de recuperación](../modulo-7/05-riesgos-operativos-recuperacion.md)
- [Capturas originales](../modulo-7/evidencias/grafana/README.md)
- [Decisión sobre distribución M6](ADR-007-distribucion-resiliencia-modulo6.md)
- [CI #217 verde](https://github.com/afat13/Proyecto-ArquitecturaSoftware/actions/runs/42318012828)

## Ratificación pendiente

Antes de marcar el ADR como **Aceptado**, consignar: participantes, objeciones, costos/riesgos aceptados, fecha y veredicto explícito del equipo. No completar esas respuestas retrospectivamente sin haberlas discutido.
