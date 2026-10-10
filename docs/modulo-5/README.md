# Evidencias del Módulo 5 — Aprende a Aprender

Se adoptó la estructura documental del repositorio UTrabajo, **adaptando el contenido al código de Aprende a Aprender**.

| Entregable | Evidencia |
|---|---|
| Subdominios | [subdominios](../dominio/subdominios.md) |
| Context Map con imagen | [Context Map Mermaid y SVG](../dominio/context-map.md) · [imagen](../dominio/context-map.svg) · [PlantUML](../dominio/context-map.puml) |
| Responsabilidades | [responsabilidades](../dominio/responsabilidades-contextos.md) |
| Comparación de integración | [síncrono vs asíncrono](../integracion/sincrono-vs-asincrono.md) |
| Contrato API actual | [OpenAPI](../integracion/contrato-api.yaml) · [descripción](../integracion/contrato-api.md) |
| Frontera seleccionada | [Tareas → Materias](../integracion/contrato-frontera-tareas-materias.md) |
| Eventos de dominio | [eventos candidatos](../integracion/eventos-candidatos.md) |
| CQRS y Event Sourcing | [análisis](../integracion/cqrs-event-sourcing.md) |
| Registro crítico de IA | [auditoría](../integracion/registro-critico-ia.md) |
| Experimento | [preregistro Spike 1 existente](../../experimentos/spike-01/00-preregistro.md) |
| ADR | [ADR 006 provisional](../adr/ADR-006-integracion-tareas-modulo5.md) |

**Pendiente:** el preregistro antiguo se enfoca en `GET /api/tasks`, mientras el nuevo análisis de frontera Tareas → Materias se enfoca en validación de materia durante la creación. No es el mismo experimento. Si se implementa un spike de esa frontera, primero debe preregistrarse en un commit anterior a cualquier código experimental. No se han inventado resultados ni cambiado el backend.
