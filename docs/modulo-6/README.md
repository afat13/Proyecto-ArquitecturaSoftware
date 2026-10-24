# Módulo 6: crecimiento, resiliencia y costo

El experimento de carga **ya se ejecutó** el 23 de octubre. Se hicieron 20 corridas sobre cinco escalones: 30, 50, 100, 150 y 200 usuarios virtuales. La semilla se verificó: 5.000 usuarios, 25.000 materias, 5.000.000 de tareas.

Los resultados registran latencia creciente y respuestas correctas. Con más carga, el p95 global subió de 144,66 ms a 1.287,58 ms, pero los GET/s no aumentaron en la misma proporción. La causa aún no está identificada porque el instrumento no separó los percentiles de GET de los logins y porque no capturamos utilización continua del generador.

## Evidencias

- [Preregistro](../../experimentos/crecimiento-m6/00-preregistro.md): commit `007defa`, anterior a las pruebas.
- [Ejecución y análisis de las 20 corridas](../../experimentos/crecimiento-m6/01-analisis-20261023.md): commit medido `815c5df5`. Los 84 archivos del experimento de crecimiento están publicados en el repositorio, con los JSON actuales depurados de tokens de sesión.
- [Matriz de fallos y resiliencia](matrices.md): separa degradación medida de hipótesis no confirmadas.
- [Comparación de estilos y costos](distribucion-costos.md).
- [Propuesta de IA y revisión](critica-ia.md).
- [ADR-007](../adr/ADR-007-distribucion-resiliencia-modulo6.md): alternativa A confirmada por el equipo; recoge el veredicto del mini-comité y sus límites experimentales.
- [Fitness function](fitness-function.md): candidata definida, sin prueba automática todavía.
- [Trazabilidad](trazabilidad.md): contiene lo disponible y lo pendiente.

## Para cerrar el módulo

Los resultados de crecimiento y la comparación de paginación están publicados. El equipo confirmó mantener Spring Boot y PostgreSQL y optimizar Tareas. Queda seleccionar, implementar y validar una fitness function en CI. Se añadió un endpoint paginado en el backend, pero Android todavía usa el endpoint original; el código sí se modificó para el experimento.
