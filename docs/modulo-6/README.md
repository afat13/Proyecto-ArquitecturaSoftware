# Módulo 6 — crecimiento, fallos, resiliencia y costo

**Estado: preparado, no medido, no cerrado.** Se conserva la línea base de M2 y se evita simular resultados de un experimento de M6.

## Orden de trabajo

1. Cerrar el Spike 1 de M5 con mediciones crudas y veredicto. Revisar [coherencia M5](../modulo-5/coherencia-spike.md).
2. Ratificar y sellar por commit el [preregistro de crecimiento](../../experimentos/crecimiento-m6/00-preregistro.md) antes de ejecutar.
3. Ejecutar con k6 contra la misma semilla en escalones de 30, 50, 100, 150 y 200 usuarios virtuales (30 enlaza con la referencia histórica); documentar qué escalones son realizables para el equipo.
4. Rellenar matrices **después** de observar fallos y revisar alternativas; distinguir observado, riesgo e inferencia.
5. Formalizar ADR de M6 con costos relativos, consecuencias y condición de reversión; someterlo a mini-comité.
6. Escoger una regla de fitness vinculada a la decisión real, automatizarla en CI y demostrar un control negativo que falle.

## Artefactos

- [Preregistro de crecimiento](../../experimentos/crecimiento-m6/00-preregistro.md).
- [Registro de mediciones y condiciones](../../experimentos/crecimiento-m6/README.md).
- [Matrices de fallos y resiliencia](matrices.md).
- [Evaluación de alternativas y costo](distribucion-costos.md).
- [Fitness function y prueba negativa](fitness-function.md).
- [Trazabilidad de corte 2](trazabilidad.md).

Un script k6 disponible no equivale a una corrida; un ADR provisional no es un veredicto del comité. Estos documentos explicitan esa diferencia.
