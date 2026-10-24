# Resultados de la comparación de paginación — 30 VUs

**Estado:** ocho JSON originales revisados y seis logs aportados. Se verificaron los resultados y se preparó una copia depurada sin `setup_data.tokens`. **La copia depurada todavía no está publicada en Git.** Los logs de las corridas 1 no se conservaron en el ZIP aportado.

Se compararon consultas del endpoint anterior `GET /api/tasks` (1.000 tareas) y del nuevo `GET /api/tasks/page?page=0&size=50` (50 tareas). Se usaron 30 VUs y 60 segundos por prueba; corrida 1 de calentamiento, corridas 2–4 medidas. Se mantuvieron las mismas cuentas de la semilla y el mismo backend en cada par según los comandos y logs aportados.

| Corrida | p95 GET anterior (ms) | GET/s anterior | p95 GET paginado (ms) | GET/s paginado |
|---:|---:|---:|---:|---:|
| 1, calentamiento | 96,70 | 360,87 | 15,90 | 3.004,06 |
| 2 | 110,28 | 300,68 | 20,53 | 2.355,80 |
| 3 | 86,10 | 394,74 | 15,50 | 3.153,38 |
| 4 | 86,68 | 391,55 | 15,92 | 3.086,81 |
| **Mediana corridas 2–4** | **86,68** | **391,55** | **15,92** | **3.086,81** |

La mediana de p95 por consulta bajó aproximadamente **81,6 %** y las solicitudes GET por segundo aumentaron aproximadamente **7,9 veces**, según estos ensayos. No hubo errores HTTP ni checks funcionales fallidos en ninguna de las ocho ejecuciones informadas.

**Límite central:** una petición anterior devuelve 1.000 tareas y una paginada devuelve 50. El número de solicitudes por segundo **no equivale a tareas transferidas por segundo**. Esta prueba favorece cargar una primera página pequeña, pero no demuestra que descargar 1.000 tareas a través de 20 páginas sea mejor. Tampoco identifica el cuello de botella principal ni prueba resultados para 100–200 VUs.

Los avisos `NativeCommandError` en PowerShell aparecen al pasar la salida informativa de Docker por `Tee-Object`; los resúmenes de k6 indican finalización normal de las seis corridas adicionales. Se revisaron los ocho resúmenes JSON y seis logs; faltan logs de calentamiento 1 y medidas continuas de recursos para atribuir cuellos de botella.

## Siguiente paso

Revisar los archivos originales para excluir `setup_data.tokens` y otras credenciales **antes** de subirlos a GitHub; los tokens ya publicados en otras evidencias no se revocan al editar la última versión. Subir los resultados depurados en `experimentos/crecimiento-m6/comparacion-paginacion/` y dejar el SHA del commit de evidencia en la trazabilidad.

El endpoint sigue siendo adicional: Android todavía llama al endpoint sin paginación. No registrar la mejora como desplegada para los estudiantes hasta adaptar cliente y pruebas.

## Verificación del ZIP

- SHA-256 del archivo original recibido: `5cf0c6bbb8468ca6989850c1ac0dfc0072b6f8b49f42ab9289c97b66add33ff1`.
- Se retiró el campo `setup_data.tokens` de los ocho JSON de la copia para publicar. Se comprobaron los seis logs y no se detectaron en ellos patrones evidentes de tokens o credenciales.
- Totales del experimento: 90.584 GET originales y 724.189 GET paginados, todos con checks exitosos (0 fallidos); 1.629.546 checks correctos entre ambos modos.
- Mediana de bytes recibidos por GET medida en corridas 2–4: aproximadamente 325 KB sin paginación y 16,6 KB con paginación.
- Evidencia pendiente de publicación: subir únicamente la copia depurada, no el ZIP original. No declarar aprobación del ADR por este ensayo.
