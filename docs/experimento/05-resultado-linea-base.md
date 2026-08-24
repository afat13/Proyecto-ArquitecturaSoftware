# Resultado de línea base

## Estado

Medición real ejecutada y conservada en el repositorio. Este documento resume los archivos crudos; no los reemplaza.

## Versión medida

- Rama: `migracion-postgresql`
- Commit medido: `0bbff1ca77b2884a6658d690f58564b2aa37da79`
- Operación: `GET /api/tasks`
- PostgreSQL: `postgres:16-alpine`
- Usuarios virtuales: 30
- Duración: 60 segundos por corrida
- Corridas: 4
- Corrida 1: calentamiento, excluida del valor final

El contexto completo de máquina y herramientas está en `experimentos/consulta-tareas/resultados/contexto.json`.

## Semilla verificada

La verificación previa a la medición registró:

| Dato | Valor |
| --- | ---: |
| Usuarios | 5.000 |
| Correos únicos | 5.000 |
| Materias | 25.000 |
| Tareas totales | 5.000.000 |
| Mínimo de tareas por usuario | 1.000 |
| Máximo de tareas por usuario | 1.000 |
| Promedio de tareas por usuario | 1.000 |
| Usuarios con exactamente 1.000 tareas | 5.000 |

La evidencia se conserva en `experimentos/consulta-tareas/resultados/verificacion-semilla.csv`.

## Resultados

| Corrida | Uso | p95 (ms) | Mediana (ms) | Promedio (ms) | Solicitudes/s | Checks fallidos |
| ---: | --- | ---: | ---: | ---: | ---: | ---: |
| 1 | calentamiento | 135,74 | 50,08 | 60,77 | 345,95 | 0 |
| 2 | válida | 103,72 | 47,70 | 53,73 | 364,45 | 0 |
| 3 | válida | 90,75 | 45,62 | 49,62 | 395,09 | 0 |
| 4 | válida | 82,93 | 41,79 | 45,59 | 419,53 | 0 |

Los valores p95 de las corridas válidas fueron:

- corrida 2: `103,71587135 ms`;
- corrida 3: `90,7544952 ms`;
- corrida 4: `82,925049 ms`.

## Línea base

El protocolo establece como línea base la mediana de los valores p95 de las corridas 2, 3 y 4.

**Línea base p95: `90,7544952 ms`, aproximadamente `90,75 ms`.**

## Validez observada

- Las cuatro corridas finalizaron y generaron salida cruda.
- No se registraron checks fallidos: 0 en las cuatro corridas.
- La semilla fue verificada antes de medir.
- La versión exacta medida quedó registrada en `contexto.json`.
- Los archivos `corrida-1.json` a `corrida-4.json` y sus logs permanecen sin reemplazar por este resumen.

El campo agregado `tasa_fallos_http` aparece como `null` en el resumen debido al formato de exportación de la métrica, por lo que no se utiliza como prueba independiente de ausencia de errores. La evidencia disponible para las validaciones de respuesta son los checks de k6, que registraron cero fallos.

## Observación

El p95 disminuyó de una corrida a la siguiente y aumentó el número de solicitudes por segundo. Esto es un comportamiento observado, pero esta medición por sí sola no permite atribuirlo a caché, JIT, PostgreSQL, Docker u otro componente.
