# Análisis de integración: Tareas → Materias

## Frontera real
`TaskController.create` usa una transacción y consulta `subject` por `subjectId` y `userId` al crear tareas. `TaskController.list` ejecuta un `JOIN` para conocer el nombre de la materia. Este es el mecanismo As-Is.

## Alternativa A: síncrona
Mantener el acceso transaccional y, si la evolución del código lo necesita, encapsular el mínimo contrato de Materias en una interfaz Java interna. Permite validar propiedad y existencia inmediatamente, sin broker, caché de estado externo ni HTTP adicional. Tiene acoplamiento al proveedor y a su disponibilidad, aunque hoy comparten proceso.

## Alternativa B: asíncrona
Materias publicaría `MateriaCreada`, `MateriaActualizada` y `MateriaEliminada`, mientras Tareas conservaría una proyección. Reduciría dependencia temporal futura, pero introduce consistencia eventual, duplicados, orden de mensajes, broker/outbox, reintentos y reconciliación.

| Criterio | Síncrono | Asíncrono |
|---|---|---|
| Validación de materia propia | En la transacción actual | Proyección quizá obsoleta |
| Dependencia temporal | Inmediata | Menor |
| Complejidad | JDBC existente | Broker, consumidor, proyección |
| Observabilidad | SQL y respuesta | Retraso, duplicados, reproceso |
| Costo | Bajo en un monolito | Alto sin necesidad demostrada |

**Preferencia provisional:** preservar la comprobación síncrona y no introducir broker. El preregistro existente de Spike 1 mide `GET /api/tasks`; no mide específicamente la frontera de creación de tarea. No debe hacerse pasar la línea base anterior por veredicto de esta integración. Ver [contrato lógico](contrato-frontera-tareas-materias.md).
