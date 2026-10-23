# ADR-007 — Distribución, costo y resiliencia en M6

**Estado:** análisis con evidencia, **pendiente de decisión y mini-comité**. Correspondencia con «ADR 4» del sílabo, sin renumerar los anteriores.

## Contexto

Aprende a Aprender ejecuta un backend Spring Boot con PostgreSQL. Tareas y Materias son fronteras lógicas dentro de la misma API y la creación de tareas usa una operación SQL local que comprueba la pertenencia de la materia.

La prueba de crecimiento M6 ([análisis](../../experimentos/crecimiento-m6/01-analisis-20261023.md), commit medido `815c5df5`) incrementó concurrencia de 30 a 200 VUs. El p95 global pasó de 144,66 a 1.287,58 ms (medianas de tres corridas), sin checks fallidos. La tasa de GET/s se mantuvo cerca de 238–250. **No conocemos el componente limitante:** faltan medidas continuas y la submétrica de GET. No convertir esta observación en una atribución causal.

## Alternativas y trade-offs

1. **Backend único con modularización y diagnóstico de capacidad:** menos infraestructura, transacciones locales y cambios acotados; sigue existiendo un despliegue compartido.
2. **Escalar API horizontalmente:** posible capacidad adicional si la API limita; requiere balanceo, varias instancias y manejo de conexiones. PostgreSQL sigue siendo compartida.
3. **Servicios independientes de Tareas y Materias:** autonomía potencial de despliegue y escalado; mayor costo operativo, consistencia y fallos parciales, sin justificación medida en esta prueba.

Comparación detallada: [distribución y costo](../modulo-6/distribucion-costos.md), [fallos y resiliencia](../modulo-6/matrices.md) y [crítica de IA](../modulo-6/critica-ia.md).

## Propuesta de revisión (no decisión aprobada)

Antes de migrar a servicios, medir por separado las consultas y el uso de recursos durante carga. Evaluar el plan SQL, límites de concurrencia y escalado del backend con pruebas comparables. Reconsiderar la distribución cuando se identifique un recurso limitante, haya necesidad real de despliegues separados o cambien restricciones del equipo.

**Consecuencia positiva potencial:** evita infraestructura no justificada y permite decisiones graduales. **Consecuencia negativa potencial:** si el límite exige aislamiento real, aplaza esa mejora. **Reversibilidad:** se conserva la API actual; se pueden extraer módulos con contratos y pruebas después, asumiendo costo de migración.

## Decisión de equipo

**Pendiente de deliberación.** Registrar alternativa elegida y descartadas, justificación a partir de resultados, costos operativos relativos, supuesto clave, condición exacta de revisión y responsables. No confundir «propuesta de revisión» con un ADR aceptado.

## Mini-comité 2

**Pendiente:** fecha, asistentes, preguntas, objeciones, cambios y veredicto (confirmada / ajustada / reconsiderada). No hay acta verificable todavía.

## Fitness function

[Definición y comprobación](../modulo-6/fitness-function.md): seleccionar propiedad una vez aprobada la decisión y demostrar violación en CI; no afirmar implementación sin evidencia.
