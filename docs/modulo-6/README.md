# Módulo 6: crecimiento y resiliencia

La idea de este módulo es medir qué pasa cuando aumentamos los usuarios concurrentes. No necesitamos volver a inventar la línea base: ya tenemos cuatro corridas de `GET /api/tasks` a 30 usuarios virtuales, con un p95 mediano de 90,75 ms. Esa medición corresponde al entorno y al commit que quedaron registrados en [la línea base](../experimento/05-resultado-linea-base.md).

Lo que todavía no tenemos son resultados válidos de crecimiento ni evidencia para justificar un cambio de arquitectura.

## Archivos de trabajo

- [Protocolo propuesto para aumentar carga](../../experimentos/crecimiento-m6/00-preregistro.md).
- [Dónde guardar las nuevas corridas](../../experimentos/crecimiento-m6/README.md).
- [Fallos que encontremos y opciones de resiliencia](matrices.md).
- [Comparación de alternativas y costos](distribucion-costos.md).
- [ADR-007](../adr/ADR-007-distribucion-resiliencia-modulo6.md).
- [Regla arquitectónica para CI](fitness-function.md).
- [Relación de evidencias para el corte](trazabilidad.md).

## Qué sigue

Primero debemos resolver la diferencia entre el Spike 1 sobre consultas y la frontera Tareas–Materias. Después se confirma el protocolo de crecimiento en un commit y se ejecutan nuevas cargas por escalones. Los resultados se guardan sin sobrescribir los archivos de la línea base.

Con esas mediciones podremos escribir la matriz de fallos y decidir si vale la pena introducir alguna táctica de resiliencia o cambiar la distribución del sistema. El ADR-007 y la fitness function deben basarse en esa decisión, no en un resultado supuesto.

El módulo no está cerrado mientras falten resultados, veredicto del mini-comité y prueba automatizada de la regla elegida.
