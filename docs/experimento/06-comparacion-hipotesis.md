# Comparación con la hipótesis inicial

## Hipótesis preregistrada

La hipótesis inicial fue registrada antes de cualquier medición y planteó observar degradación del tiempo de respuesta de `GET /api/tasks` bajo carga concurrente con una cuenta que contenía 10.000 tareas distribuidas de forma desigual.

Ese documento se conserva sin modificaciones en `docs/experimento/01-hipotesis-inicial.md`.

## Cambio del diseño antes de medir

Antes de ejecutar la primera medición real, la semilla y el protocolo se modificaron para representar crecimiento global del sistema y concurrencia con identidades independientes:

- 5.000 usuarios distintos;
- 1.000 tareas por usuario;
- 5 materias por usuario;
- 5.000.000 de tareas totales;
- 30 usuarios virtuales con una cuenta distinta por VU.

Por esta razón, la medición ejecutada no constituye una prueba literal del escenario original de 1 usuario con 10.000 tareas. La hipótesis original no se reescribe retroactivamente.

## Resultado observado del diseño revisado

La línea base obtenida para `GET /api/tasks` fue:

**p95 = 90,75 ms**

usando la mediana de los p95 de las corridas válidas 2, 3 y 4:

- 103,72 ms;
- 90,75 ms;
- 82,93 ms.

Las cuatro corridas registraron cero checks fallidos.

## ¿Se confirma o se refuta la hipótesis inicial?

No se clasifica como confirmada ni refutada de forma estricta, porque cambió una condición central de la semilla antes de la medición: cada cuenta consultada pasó de 10.000 tareas en la hipótesis inicial a 1.000 tareas en una base global de 5.000.000 de tareas.

Lo que sí puede afirmarse con la evidencia obtenida es que, bajo el diseño revisado, `GET /api/tasks` respondió con un p95 de aproximadamente 90,75 ms para 30 usuarios virtuales concurrentes, cada uno consultando sus 1.000 tareas dentro de una base poblada con 5.000 usuarios y 5.000.000 de tareas.

## Relación con la expectativa de degradación

El resultado no muestra por sí solo una degradación severa bajo las condiciones medidas. Sin una línea base comparable con menor volumen global, tampoco es válido afirmar cuánto degradó el sistema debido al crecimiento de datos.

Para medir degradación de forma causal o comparativa se necesitaría un segundo escenario controlado que cambie únicamente el volumen de datos y mantenga constantes las demás condiciones.

## Conclusión

La primera medición establece una línea base reproducible del diseño revisado. Su valor principal para el Corte 1 es proporcionar un punto de referencia medido, trazable al commit y acompañado por datos crudos. No se atribuye el resultado a una causa específica y no se modifica la hipótesis original para hacerla coincidir con los datos.
