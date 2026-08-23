# Hipótesis inicial de rendimiento

## Estado

Documento de preregistro. Esta hipótesis se registra antes de ejecutar cualquier medición de línea base del escenario principal y no debe modificarse después para hacerla coincidir con los resultados.

## Fenómeno que se espera observar

Con una cuenta que contiene 10.000 tareas distribuidas de forma desigual entre sus materias, esperamos observar degradación del tiempo de respuesta al consultar el listado completo de tareas bajo carga concurrente.

La observación se realizará sobre la operación HTTP:

```text
GET /api/tasks
```

No se atribuye de antemano el comportamiento a PostgreSQL, a la consulta SQL, al backend Spring Boot, a la red ni a otro componente. En esta etapa se registra únicamente el fenómeno esperado y la forma de observarlo.

## Métrica principal

La métrica principal será el percentil 95 (p95) del tiempo de respuesta HTTP de `GET /api/tasks`, expresado en milisegundos.

Como métricas auxiliares se conservarán:

- mediana del tiempo de respuesta;
- promedio del tiempo de respuesta;
- solicitudes por segundo;
- porcentaje de peticiones fallidas;
- códigos de respuesta HTTP.

## Semilla necesaria para que el fenómeno pueda aparecer

La cuenta de prueba deberá tener:

- 1 usuario;
- 8 materias;
- 10.000 tareas;
- una distribución deliberadamente desigual de las tareas entre materias.

Distribución preregistrada:

| Grupo | Tareas | Proporción |
| --- | ---: | ---: |
| Materia 1 | 8.000 | 80 % |
| Materias 2 y 3 | 1.500 en total | 15 % |
| Materias 4 a 8 | 500 en total | 5 % |
| **Total** | **10.000** | **100 %** |

La distribución deberá verificarse mediante una consulta SQL antes de considerar válida una corrida.

## Carga preregistrada

Para la primera línea base se utilizarán inicialmente:

- 30 usuarios virtuales concurrentes;
- 60 segundos por corrida;
- cuatro corridas bajo condiciones comparables;
- la primera corrida se tratará como calentamiento y no se utilizará para el resultado final;
- las corridas 2, 3 y 4 serán las corridas válidas si no presentan errores de instrumentación o de respuesta.

El número de línea base será la mediana de los valores p95 obtenidos en las corridas válidas.

## Criterios de validez

Una corrida no se considerará exitosa simplemente por haber terminado. Antes de utilizarla se comprobará:

1. que el inicio de sesión haya respondido correctamente;
2. que `GET /api/tasks` responda con código HTTP 200;
3. que la respuesta contenga tareas;
4. que la semilla siga teniendo el volumen y la distribución declarados;
5. que se conserven los resultados crudos de la herramienta;
6. que se registre el hash del commit exacto medido;
7. que se documenten las condiciones de la máquina y de la base de datos.

## Lo que esta hipótesis no afirma

Este documento no propone una optimización ni una causa. Un resultado alto o bajo no demostrará por sí mismo que PostgreSQL, un índice, el backend o la red sean la causa. Cualquier explicación causal requerirá evidencia adicional.

## Estado de medición al registrar esta hipótesis

No se ha ejecutado todavía la medición de línea base descrita en este documento. Los archivos de instrumentación existentes se consideran preparación del experimento; no contienen resultados de ejecución.
