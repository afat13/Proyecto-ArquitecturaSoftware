# Eventos candidatos — Aprende a Aprender

Un evento registra un hecho del dominio **ya ocurrido**. El backend actual no implementa un broker ni publicación de estos eventos.

| Evento conceptual | Contexto productor | Consumidor potencial | Observación |
|---|---|---|---|
| MateriaCreada | Materias | Tareas y Retos | El alta existe, pero hoy se consulta PostgreSQL directamente. |
| MateriaEliminada | Materias | Tareas y Retos | La replicación eventual puede permitir referencias obsoletas. |
| MateriaSincronizada | Sincronización | Reportes | Solo si produjo un cambio significativo. |
| TareaCompletada | Tareas | Analítica futura | `TaskController.updateStatus` modifica el estado. |
| RetoDiarioCompletado | Retos | Analítica futura | `ChallengeController.complete` registra progreso. |

**Rechazados:** `BotonGuardarPulsado` (UI), `RegistroInsertado` (operación técnica), `CertificadoEmitido` (flujo no constatado) y `PreguntasGuardadas` (CRUD sin consumidor demostrado).

Antes de implementar mensajería se necesita definir evento inmutable, identificador, instante, esquema versionado, entrega confiable, idempotencia y reprocesamiento. No se introducen estas tecnologías sin evidencia. Ver [registro crítico](registro-critico-ia.md).
