# Distribución y costos — análisis previo, no veredicto

**Estado: alternativas identificadas; decisión pendiente de datos y mini-comité.**

| Alternativa | Potencial beneficio | Costo operativo relativo | Complejidad de equipo | Riesgos para la frontera Tareas–Materias |
|---|---|---|---|---|
| Backend modular único con PostgreSQL | Transacciones locales; operaciones conocidas | Bajo respecto de las otras propuestas | Menor número de despliegues; requiere control de dependencias internas | Acoplamiento lógico y recursos compartidos |
| Mismo backend con escalado horizontal y topología de infraestructura controlada | Más instancias sin separar contexto de negocio | Medio (instancias, balanceador, sesiones, monitoreo) | Requiere reproducibilidad y gestión de conexiones a base de datos | Persistencia sigue siendo dependencia compartida |
| Servicios Tareas y Materias separados | Aislamiento de escalado/despliegue **potencial** | Alto en comparación (red, observabilidad, despliegue y datos) | Contratos de comunicación, fallos parciales y consistencia | Validación local de `subject_id` en `TaskController.create` dejaría de ser local |

Estos son **órdenes comparativos cualitativos**, no un presupuesto financiero ni un resultado de benchmarking. Evaluar costo de infraestructura, despliegue, soporte, incidentes y disponibilidad de personas.

## Antes de elegir
Vincular cada alternativa con resultados de crecimiento válidos, cuellos de botella **observados**, capacidad y costo operativo. El rendimiento insuficiente de una consulta no demuestra por sí mismo la necesidad de microservicios.

## Registro de decisión
- Alternativa elegida: **pendiente**.
- Evidencia experimental y limitaciones: **pendiente**.
- Justificación, consecuencias, reversibilidad, supuestos y umbral de revisión: completar [ADR-007](../adr/ADR-007-distribucion-resiliencia-modulo6.md).
- Mini-comité 2: fecha, participantes, objeciones y veredicto: **pendiente**.
