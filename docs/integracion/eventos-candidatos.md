# Catálogo evaluado de eventos de dominio

El código actual usa operaciones REST/JDBC; los eventos siguientes son **candidatos conceptuales**, no publicaciones implementadas.

| Candidato en pasado | Hecho de negocio | Posible interesado | Decisión |
|---|---|---|---|
| TareaCompletada | El usuario terminó una tarea | Analítica futura de progreso | Candidato; no existe consumidor que exija broker |
| MateriaSincronizada | Se incorporó/actualizó información externa | Diagnóstico de sincronización | Candidato sujeto a distinguir importación de modificación efectiva |
| RetoDiarioCompletado | Se terminaron actividades de práctica | Analítica futura | Candidato; estado ya persiste en backend |
| PreguntasGuardadas | Se persistieron preguntas | Ninguno identificado | Rechazado como evento de negocio por ahora |
| BotonRetoPresionado | Interacción de UI | Ninguno | Rechazado: detalle de interfaz |
| RegistroInsertado | Acción CRUD | Ninguno | Rechazado: detalle técnico |

## Si se introducen eventos
Propuesta de sobre (no implementada): `eventId` UUID, `eventType`, `occurredAt` UTC, `aggregateId`, `userId`, `schemaVersion`, `payload`. Para entregar al menos una vez, consumidores deberán registrar `eventId` procesados, evitar efectos duplicados, aplicar reintentos y distinguir orden/consistencia eventual.

## Auditoría de IA
Las sugerencias de eventos no equivalen a hechos observados. Ningún nombre anterior debe presentarse como clase, tópico o tabla ya existente.
