# Módulo 4 — revisión de estilos y diseño modular

Este índice relaciona las evidencias del módulo con el código actual. No convierte ADR históricos en decisiones tomadas durante las semanas 7–8.

## Comparación de estilos

| Alternativa | Ventaja para el sistema | Costo y renuncia | Evidencia pendiente |
| --- | --- | --- | --- |
| Monolito con responsabilidades delimitadas y API REST | Conserva las transacciones locales, el despliegue del backend y el acceso actual a PostgreSQL | Mantiene un único punto de despliegue y exige disciplina para proteger fronteras | Medir acoplamiento y validar módulos con pruebas |
| Separar servicios para Tareas y Materias | Escalado y despliegue potencialmente independientes | Red, latencia, contratos internos, despliegues y fallos parciales; la transacción de `TaskController.create` dejaría de ser local | Experimento representativo y estimación operativa |
| Cliente Android conectado directamente a persistencia | Elimina una capa de API | Exposición de persistencia y control de acceso, acoplamiento al esquema | No existe evidencia que justifique eliminar API y seguridad |

**Drivers observables:** aislamiento de usuarios en `TaskController`, rendimiento de consultas, operabilidad de un backend Spring Boot con PostgreSQL, seguridad y capacidad del equipo. La línea base disponible mide `GET /api/tasks`; no demuestra por sí sola el mejor estilo arquitectónico.

**Situación actual:** se conserva la implementación existente como punto de partida de la comparación, **no** como veredicto del mini-comité ni como decisión nueva demostrada. La distribución física requiere demostrar que sus beneficios compensan complejidad y costo.

## Diseño modular actual y fronteras por validar

- Android: pantallas, navegación, ViewModels y repositorios cliente; consume la API mediante Retrofit.
- API: controladores de autenticación, perfil, materias, tareas y retos; servicios de autenticación y filtro de token.
- Persistencia: PostgreSQL, migraciones Flyway y consultas con `JdbcClient`.
- Tareas–Materias: `TaskController.create` verifica pertenencia de la materia dentro de una operación SQL local; `list` usa `JOIN subject`. Es una frontera lógica propuesta, no dos servicios independientes.
- IA local: integración del modelo Gemma en Android, distinta de las operaciones REST de tareas.

Para auditar dependencias y contratos, consultar [C4 y trazabilidad](../c4/04-trazabilidad-codigo.md), [contextos](../dominio/responsabilidades-contextos.md) y [contrato de frontera](../integracion/contrato-frontera-tareas-materias.md).

## Decisiones y ADR

Los [ADR 001–005](../adr/README.md) documentan decisiones existentes de forma retrospectiva. ADR-001 (persistencia PostgreSQL) y ADR-002 (API entre Android y PostgreSQL) se emplean como referencias para contrastar alternativas de estilo, pero **no** sustituyen dos decisiones deliberadas por el equipo en M4 con veredicto documentado.

## Crítica de una arquitectura sugerida por IA

**Propuesta hipotética a evaluar (no salida histórica de un modelo):** separar de inmediato Tareas y Materias en microservicios con comunicación HTTP interna y bases de datos independientes.

La propuesta tiene atractivo por autonomía de despliegue. Sin embargo, no aporta una medida de acoplamiento que haga necesaria esa separación, desplaza validaciones transaccionales locales a una frontera de red y aumenta operaciones, contratos y posibles fallos parciales. Una recomendación de IA debería sustentarse en restricciones y mediciones del sistema, no en la popularidad del estilo. Esta crítica **no equivale** a una auditoría fechada de una respuesta real de IA; para satisfacer esa evidencia, adjuntar prompt, respuesta y contraste con código.

## Acta del mini-comité 1

**Estado: pendiente de sesión y validación humana.** Registrar participantes, fecha, dos decisiones sometidas, objeciones, cambios pedidos, veredicto de cada ADR (confirmada / ajustada / reconsiderada), evidencia consultada y commit posterior. No asignar una aprobación retroactiva.

## Criterio de cierre

M4 seguirá **parcial** hasta disponer de dos ADR evaluados con trazabilidad contemporánea, propuesta real de IA criticada y acta de comité. Esta página permite localizar las brechas sin fabricar actas ni decisiones.
