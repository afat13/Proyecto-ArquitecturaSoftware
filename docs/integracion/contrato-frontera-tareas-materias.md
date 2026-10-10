# Contrato lógico propuesto: Tareas consume Materias

**Estado:** propuesta de análisis; no implementado como endpoint ni servicio separado.

- **Proveedor:** Materias.
- **Consumidor:** Tareas.
- **Propósito:** verificar que la materia referida por una tarea exista y pertenezca al usuario actual.
- **Entrada mínima:** `subjectId: UUID` y `userId: UUID` proveniente de autenticación, nunca confianza en ID de usuario arbitrario del cliente.
- **Salida lógica:** `subjectId` y, cuando se precise para lectura, `name`.
- **Resultados:** referencia autorizada, referencia inexistente/no perteneciente, fallo de persistencia.
- **No permite:** modificar una materia, recuperar participantes ni exponer temas o datos personales desde Tareas.

## Mecanismo hoy
No hay contrato HTTP interno entre los dos módulos. `TaskController.create` hace `INSERT ... SELECT FROM subject ... WHERE user_id` dentro de una transacción local. `TaskController.list` ejecuta `JOIN subject` para mostrar `subject_name`.

## Alternativa de encapsulación
Si se justifica por evolución del código, introducir interfaz Java interna `MateriaReferenciaProvider`, evitando la creación artificial de `/internal/*` HTTP y respetando la transacción.

El contrato público actual de cliente a API se documenta en [contrato-api.yaml](contrato-api.yaml). **Versionamiento:** hoy usa `/api/tasks` sin prefijo v1; cambios incompatibles exigirían plan de migración y compatibilidad.

Este archivo define fronteras y comportamiento deseado, no afirma implementación ni resultados del Spike 1.
