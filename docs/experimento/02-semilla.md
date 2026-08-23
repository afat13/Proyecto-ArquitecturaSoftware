# Semilla del experimento

## Objetivo

Construir un conjunto de datos reproducible para observar el comportamiento de `GET /api/tasks` cuando una cuenta contiene un volumen alto de tareas con distribución desigual entre materias.

## Archivo ejecutable

`experimentos/consulta-tareas/seed.sql`

## Usuario de prueba

`estudiante@aprende.local`

El ejecutor del experimento crea o valida esta cuenta antes de cargar los datos.

## Volumen

- 1 usuario.
- 8 materias.
- 10.000 tareas.

## Distribución

| Grupo | Cantidad | Porcentaje |
| --- | ---: | ---: |
| Materia 1 | 8.000 | 80 % |
| Materia 2 | 750 | 7,5 % |
| Materia 3 | 750 | 7,5 % |
| Materias 4 a 8 | 100 cada una | 5 % total |
| Total | 10.000 | 100 % |

La distribución no pretende representar a todos los estudiantes. Es una semilla controlada diseñada para que el fenómeno de concentración y crecimiento de datos pueda aparecer durante la medición.

## Reproducibilidad

Antes de insertar la semilla, el script elimina las materias del usuario de experimento. Las relaciones `ON DELETE CASCADE` eliminan las tareas asociadas. Luego recrea las ocho materias y las 10.000 tareas.

Una nueva ejecución no acumula datos de corridas anteriores.

## Verificación numérica

La distribución se comprueba con:

`experimentos/consulta-tareas/verificar-semilla.sql`

La consulta agrupa las tareas por materia y calcula cantidad y porcentaje. El ejecutor guarda la evidencia en:

`experimentos/consulta-tareas/resultados/verificacion-semilla.csv`

## Condición de validez

No debe aceptarse una corrida como válida si la verificación no demuestra las 10.000 tareas y la distribución esperada.
