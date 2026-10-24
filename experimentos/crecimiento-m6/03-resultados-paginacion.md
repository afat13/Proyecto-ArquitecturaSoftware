# Resultados de la comparación de paginación — 30 VUs

**Estado:** comparación analizada a partir de los resúmenes de consola compartidos por el equipo. Los ocho JSON y sus logs permanecen en el computador de ejecución y **todavía no están publicados en Git**. No confundir esta tabla con evidencia cruda versionada.

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

Los avisos `NativeCommandError` en PowerShell aparecen al pasar la salida informativa de Docker por `Tee-Object`; los resúmenes de k6 indican finalización normal de las seis corridas adicionales. Para una auditoría completa se requieren los ocho resúmenes JSON, logs y configuración de la máquina.

## Siguiente paso

Revisar los archivos originales para excluir `setup_data.tokens` y otras credenciales **antes** de subirlos a GitHub; los tokens ya publicados en otras evidencias no se revocan al editar la última versión. Subir los resultados depurados en `experimentos/crecimiento-m6/comparacion-paginacion/` y dejar el SHA del commit de evidencia en la trazabilidad.

El endpoint sigue siendo adicional: Android todavía llama al endpoint sin paginación. No registrar la mejora como desplegada para los estudiantes hasta adaptar cliente y pruebas.
