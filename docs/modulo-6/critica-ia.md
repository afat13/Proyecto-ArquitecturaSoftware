# Propuesta de microservicios de IA y revisión — M6

## Solicitud a IA

«Propón una estrategia para convertir Aprende a Aprender (Android, API Spring Boot y PostgreSQL) en microservicios. Explica separación de responsabilidades, beneficios, riesgos y costos».

## Respuesta resumida de IA

Separar progresivamente autenticación, materias y tareas en servicios desplegables de forma independiente; definir contratos de comunicación, usar un punto de entrada común y añadir balanceo y observabilidad. Migrar por etapas.

Esta es **una propuesta de IA**, no código implementado ni una decisión aprobada.

## Qué respaldan nuestras mediciones

El experimento del 23 de octubre registra una mediana de p95 global de 144,66 ms a 30 VUs y 1.287,58 ms a 200 VUs. Los GET/s no crecieron en la misma proporción (237,97 y 242,61 como medianas de extremos). Hubo cero checks fallidos entre los 321.106 GET realizados.

**A favor:** la recomendación de aclarar responsabilidades y mejorar observabilidad es razonable. Ya tenemos un Context Map y fronteras lógicas que pueden revisarse sin desplegar microservicios.

**En contra de migrar directamente:** no se midió el componente limitante. Separar Tareas y Materias convertiría las validaciones locales de `TaskController.create` en un problema de comunicación y consistencia distribuida. La propuesta no cuantifica infraestructura adicional ni disponibilidad del equipo.

**Límite de la prueba:** el p95 reportado combina GET con logins porque k6 no exportó la submétrica exclusiva de GET. Tampoco registramos recursos del generador durante la carga.

## Evaluación

La propuesta de IA **no queda respaldada como decisión de migración** por estas mediciones. Primero debemos medir el recurso limitante, evaluar mejoras simples y comprobar la necesidad de desplegar capacidades por separado. El equipo debe contrastarlo con sus restricciones y registrar su veredicto en [ADR-007](../adr/ADR-007-distribucion-resiliencia-modulo6.md).
