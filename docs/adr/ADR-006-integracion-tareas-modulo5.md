# ADR 006 (tercera decisión del Módulo 5) — Integración de tareas mediante REST síncrono

- **Estado:** propuesto; cierre pendiente del Spike 1.
- **Contexto:** Aprende a Aprender es una aplicación Android con API Spring Boot y PostgreSQL. La lista de tareas se consulta mediante `GET /api/tasks`, y el backend restringe la consulta al usuario autenticado. El sistema ya tiene un contrato HTTP consumido por Retrofit.
- **Drivers:** respuesta de UI, corrección, aislamiento entre usuarios, operabilidad, menor complejidad y rendimiento contrastable.
- **Evidencia:** `ApiService.kt`, `TaskRepository.kt`, `TaskController.java`, `docs/experimento/05-resultado-linea-base.md` y `experimentos/consulta-tareas/resultados/`.

## Alternativas
1. **Conservar REST síncrono**: interfaz ya implementada, respuesta inmediata, acoplamiento temporal a la API. Permite medición con herramientas existentes.
2. **Eventos asíncronos con vista de lectura**: desacoplamiento temporal potencial, pero necesita broker/proyecciones, idempotencia, recuperación y tratamiento de consistencia eventual. No hay consumidor demostrado para ese mecanismo en la consulta interactiva.

## Decisión provisional
Mantener REST síncrono para la consulta de tareas y documentar la frontera. **No es una aprobación experimental final**. Revisar después del Spike 1 preregistrado en `experimentos/spike-01/00-preregistro.md`.

## Consecuencias
Positivas: se aprovecha infraestructura existente, contrato simple y observabilidad por HTTP/k6. Negativas: el cliente depende de disponibilidad y tiempo de respuesta de la API; deben gestionarse errores, expiración de sesión y cambios incompatibles del contrato.

## Supuestos, reversibilidad y revisión
Se asume consulta interactiva con necesidad de datos actuales y carga comparable a la referencia. Si el nuevo spike refuta la hipótesis, revisar consulta/índices y escenarios antes de considerar mensajería. Una arquitectura de eventos sería reversible mediante coexistencia de contratos, pero costaría datos proyectados y nuevos mecanismos operativos.

## Resultado del spike
**No ejecutado**. Los 90,75 ms publicados corresponden a una línea base anterior, no al spike nuevo. Este ADR debe pasar a aceptado o rechazado únicamente después de documentar resultados y veredicto con referencias a commits.

## Trazabilidad
[Context Map](../dominio/context-map.puml) · [Comparativa](../integracion/sincrono-vs-asincrono.md) · [Contrato](../integracion/contrato-api.md) · [Spike](../../experimentos/spike-01/00-preregistro.md).
