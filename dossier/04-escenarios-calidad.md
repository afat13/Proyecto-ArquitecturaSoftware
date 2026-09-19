# 04 — Escenario de calidad y línea base

## Hipótesis original

Antes de medir dejamos registrada una hipótesis sobre `GET /api/tasks` con un usuario que tenía 10.000 tareas distribuidas de forma desigual.

Ese archivo sigue en:

`docs/experimento/01-hipotesis-inicial.md`

No lo cambiamos después.

## Qué cambió antes de la medición

Antes de hacer la primera corrida real cambiamos la semilla para probar concurrencia con cuentas distintas:

- 5.000 usuarios;
- 5 materias por usuario;
- 1.000 tareas por usuario;
- 5.000.000 de tareas en total;
- 30 VU con una cuenta distinta por VU.

Por eso no podemos decir que la hipótesis original de 10.000 tareas quedó confirmada o refutada. Lo que sí tenemos es una línea base del escenario nuevo.

## Cómo se ejecuta

```powershell
python .\experimentos\consulta-tareas\ejecutar_experimento.py
```

El script levanta la base y la API, carga la semilla, verifica los conteos, registra el commit y ejecuta las cuatro corridas.

## Resultados

| Corrida | Uso | p95 |
| ---: | --- | ---: |
| 1 | calentamiento | 135,74 ms |
| 2 | válida | 103,72 ms |
| 3 | válida | 90,75 ms |
| 4 | válida | 82,93 ms |

No hubo checks fallidos.

La línea base se calcula con la mediana de los p95 de las corridas 2, 3 y 4:

**90,7544952 ms**

## Qué podemos afirmar

Podemos afirmar cómo respondió `GET /api/tasks` bajo esas condiciones.

No podemos afirmar todavía:

- que PostgreSQL sea la causa del resultado;
- cuánto empeoró frente a una base pequeña;
- que el mismo resultado se repita por Internet;
- que una cuenta con 10.000 tareas se comporte igual.
