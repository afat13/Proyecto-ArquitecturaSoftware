# ADR-007 — Evaluación de distribución y resiliencia (decisión M6)

**Estado: borrador pendiente de crecimiento y mini-comité.** Este archivo **no registra una decisión aprobada**.

## Contexto
Backend Java/Spring Boot con PostgreSQL, tareas y materias en la misma API y operaciones transaccionales locales. Referencia histórica de `GET /api/tasks`: p95 mediano de 90,7544952 ms bajo 30 VUs en un entorno específico. Este valor no identifica un cuello de botella de crecimiento.

## Drivers
Latencia bajo carga, corrección de respuestas, disponibilidad, simplicidad de operación, costo de equipo e infraestructura, claridad de fronteras y reversibilidad.

## Alternativas por evaluar
1. Monolito modular, endureciendo límites internos y observabilidad.
2. Escalado horizontal del backend con PostgreSQL compartido.
3. Servicios independientes para capacidades justificadas, incorporando contratos remotos y aislamiento de persistencia.

[Comparación de costos y riesgos](../modulo-6/distribucion-costos.md).

## Evidencia requerida para decidir
[Preregistro](../../experimentos/crecimiento-m6/00-preregistro.md), corridas crudas, validez del generador, [matriz de fallos](../modulo-6/matrices.md) y tácticas verificables.

## Decisión y consecuencias
**Pendiente de resultados.** Registrar qué se adopta y descarta; beneficios, desventajas, costo operativo relativo, migración, reversibilidad y condiciones que harían reconsiderar la decisión.

## Mini-comité técnico 2
**No realizado / sin evidencia en este registro**. Registrar fecha, participantes, objeciones, cambios y veredicto: confirmada / ajustada / reconsiderada.

## Fitness function
**Aún sin propiedad elegida ni control negativo demostrado.** Relacionar la propiedad protegida con el resultado de este ADR; ver [diseño de fitness](../modulo-6/fitness-function.md).
