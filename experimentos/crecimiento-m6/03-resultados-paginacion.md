# Resultados de la comparación de paginación — 30 VUs

**Estado:** evidencia depurada publicada y verificada en `main`: ocho JSON y seis logs en [comparacion-paginacion/](comparacion-paginacion/). Los JSON no contienen `setup_data.tokens`; faltan los logs de calentamiento de la corrida 1, ejecutada por separado.

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

La evidencia cruda depurada está subida. Aún falta registrar el SHA exacto de la versión medida de backend y de k6, así como métricas continuas de recursos si se quiere atribuir el cuello de botella. Adaptar Android requiere una estrategia de filtros/consultas en el servidor para no dejar fuera tareas de hoy, vencidas y por materia; una primera página de 50 por sí sola sería incorrecta.

## Verificación del ZIP

- SHA-256 del archivo original recibido: `5cf0c6bbb8468ca6989850c1ac0dfc0072b6f8b49f42ab9289c97b66add33ff1`.
- Se retiró el campo `setup_data.tokens` de los ocho JSON de la copia para publicar. Se comprobaron los seis logs y no se detectaron en ellos patrones evidentes de tokens o credenciales.
- Totales del experimento: 90.584 GET originales y 724.189 GET paginados, todos con checks exitosos (0 fallidos); 1.629.546 checks correctos entre ambos modos.
- Mediana de bytes recibidos por GET medida en corridas 2–4: aproximadamente 325 KB sin paginación y 16,6 KB con paginación.
- Evidencia depurada publicada en el repositorio; verificada la ausencia de `setup_data.tokens` en los ocho JSON de `main`. No declarar aprobación del ADR por este ensayo.
