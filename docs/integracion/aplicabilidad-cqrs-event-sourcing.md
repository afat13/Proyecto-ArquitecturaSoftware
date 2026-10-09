# Aplicabilidad de CQRS y Event Sourcing

## CQRS
**Problema potencial:** consultas de tareas con mucha lectura y reglas de escritura diferentes. **Evidencia disponible:** un único endpoint `GET /api/tasks`, controladores REST, JDBC y mediciones históricas; no se ha demostrado que lecturas y escrituras requieran modelos independientes. **Costos:** proyección de lectura, sincronización, pruebas adicionales y posible consistencia eventual. **Decisión:** no introducir CQRS ahora. Separar métodos de consulta y comando no demuestra por sí solo una arquitectura CQRS.

## Event Sourcing
**Problema potencial:** reconstruir todo el historial de estados de tarea o reto. **Evidencia disponible:** persistencia relacional de estado actual mediante PostgreSQL; no se observa un requerimiento verificable de reconstrucción por secuencia de eventos. **Costos:** evolución/versionado de eventos, reconstrucción de estado, migración, reprocesamiento y operación. **Decisión:** no incorporarlo. Registrar eventos futuros tampoco obligaría a usar Event Sourcing.

## Condiciones de revisión
Reconsiderar CQRS si pruebas reproducibles muestran exigencias divergentes de lectura/escritura que las optimizaciones simples no resuelven. Reconsiderar Event Sourcing si el negocio exige auditoría histórica completa y se definen retención, privacidad, versionamiento y reconstrucción.

La decisión evita introducir complejidad no respaldada por los escenarios de calidad actuales.
