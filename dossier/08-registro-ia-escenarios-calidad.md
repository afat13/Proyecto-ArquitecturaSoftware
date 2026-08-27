# 08 — Registro de escenarios de calidad sugeridos por IA

Este documento conserva cómo algunas propuestas iniciales de IA fueron revisadas y reformuladas por el equipo para convertirlas en escenarios verificables y coherentes con el sistema real.

| Propuesta inicial de IA | Problema detectado por el equipo | Reformulación adoptada |
| --- | --- | --- |
| “La consulta de tareas debe ser rápida.” | La propuesta es demasiado genérica. No identifica una operación concreta, cantidad de datos, concurrencia, duración de la carga ni una métrica que permita verificar objetivamente el comportamiento del sistema. | Se definió como operación `GET /api/tasks`, con una semilla de 5.000 usuarios y 1.000 tareas por usuario, para un total de 5.000.000 de tareas. La carga se ejecutó con 30 usuarios virtuales durante 60 segundos por corrida. Se utilizó el p95 del tiempo de respuesta HTTP como métrica principal. La línea base observada fue aproximadamente **90,75 ms**. |
| “Realizar el experimento con un único usuario que tenga 10.000 tareas.” | Aunque permitía evaluar una consulta con un usuario de gran volumen, el equipo consideró que no representaba suficientemente el escenario de concurrencia multiusuario que quería evaluar ni el volumen global de información que podía ejercer presión sobre la aplicación y su backend. | Antes de realizar la medición real, el diseño experimental se reformuló a **5.000 usuarios**, cada uno con **1.000 tareas** distribuidas entre 5 materias, para un total de **5.000.000 de tareas**. Durante la prueba se utilizaron 30 VUs y cada VU inició sesión con una cuenta distinta. |
| “Los datos deben ser seguros.” | No especifica quién intenta acceder, qué recurso debe protegerse ni cuál debe ser la respuesta observable del sistema. | Se concreta en mantener la autenticación mediante token Bearer, la separación Android → API REST → PostgreSQL y la ausencia de credenciales PostgreSQL en el cliente móvil. |
| “El sistema debe ser escalable.” | Es demasiado amplio y mezcla rendimiento, capacidad, despliegue y disponibilidad sin una condición verificable. | Para este corte se limita la evaluación al comportamiento de `GET /api/tasks` bajo el escenario experimental definido. La escalabilidad horizontal o distribuida queda fuera del alcance medido y requeriría un experimento diferente. |

## Evolución de la propuesta experimental

La primera propuesta considerada concentraba un volumen elevado de tareas en una única cuenta. El equipo revisó esa sugerencia y concluyó que, para el objetivo de someter la arquitectura a una carga más representativa de múltiples usuarios concurrentes, era preferible distribuir el volumen.

Por esta razón, antes de ejecutar la medición real, el escenario se reformuló a 5.000 usuarios con 1.000 tareas por usuario. La hipótesis inicial se conserva sin modificaciones como evidencia histórica de la evolución del diseño experimental.

## Resultado de la reformulación

El escenario finalmente ejecutado quedó definido así:

- operación: `GET /api/tasks`;
- usuarios en la semilla: 5.000;
- tareas por usuario: 1.000;
- total global: 5.000.000 de tareas;
- carga: 30 VUs con identidades distintas;
- duración: 60 segundos por corrida;
- corridas: 4;
- corrida 1: calentamiento y excluida del resultado final;
- métrica principal: p95;
- línea base observada: aproximadamente **90,75 ms**.

La reformulación muestra que las propuestas de IA fueron usadas como punto de partida y luego ajustadas con criterios de trazabilidad, reproducibilidad y representatividad del escenario medido.
