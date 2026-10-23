# Distribución y costos con los datos de M6

El [experimento](../../experimentos/crecimiento-m6/01-analisis-20261023.md) muestra degradación de latencia con más VUs, sin respuestas incorrectas. No identifica si el problema está en la consulta SQL, la API, la máquina o el generador. Por eso no podemos presentar microservicios como solución demostrada.

| Alternativa | Qué podría aportar | Infraestructura (relativo) | Trabajo de operación/equipo | Riesgo |
|---|---|---|---|---|
| Mantener backend único y medir consultas/recursos | Atacar un límite comprobable sin nueva topología | Bajo | Bajo–medio (perfilado, tests) | Que el problema esté fuera de la consulta y no mejore |
| Escalado horizontal de la API | Más capacidad si la API limita primero | Medio: más instancias y balanceador | Medio: despliegue, conexiones, monitoreo | La base sigue compartida y puede convertirse en límite |
| Separar Tareas y Materias en servicios | Despliegues y escalado autónomos potenciales | Alto: servicios, red y monitoreo | Alto: contrato remoto, consistencia y fallos parciales | Se pierde la simplicidad de la validación SQL transaccional local |

**Comparación cualitativa**, no presupuesto real. La necesidad de operaciones separadas no se desprende de estos datos. La opción de menor complejidad sirve como punto de comparación, pero el equipo debe confirmar o reconsiderar la decisión tras analizar recursos y costo real.

La propuesta de IA se comenta en [crítica](critica-ia.md). El [ADR-007](../adr/ADR-007-distribucion-resiliencia-modulo6.md) debe incorporar el veredicto del mini-comité y el criterio de revisión.
