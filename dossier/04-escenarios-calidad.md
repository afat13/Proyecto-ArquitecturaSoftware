# 04 — Escenario principal y línea base

## Hipótesis preregistrada

La hipótesis original planteó `GET /api/tasks` con 1 usuario, 8 materias y 10.000 tareas distribuidas de forma desigual, 30 VU, cuatro corridas de 60 s y p95 como métrica. Se conserva en `docs/experimento/01-hipotesis-inicial.md`.

## Cambio antes de medir

El diseño finalmente ejecutado cambió a 5.000 usuarios, 5 materias y 1.000 tareas por usuario, para 5.000.000 de tareas. Cada uno de los 30 VU usó una cuenta distinta. Por eso la hipótesis original no puede clasificarse estrictamente como confirmada o refutada.

## Método

```powershell
python .\experimentos\consulta-tareas\ejecutar_experimento.py
```

## Resultado

Commit medido documentado: `0bbff1ca77b2884a6658d690f58564b2aa37da79`.

| Corrida | Uso | p95 | Checks fallidos |
| ---: | --- | ---: | ---: |
| 1 | calentamiento | 135,74 ms | 0 |
| 2 | válida | 103,72 ms | 0 |
| 3 | válida | 90,75 ms | 0 |
| 4 | válida | 82,93 ms | 0 |

**Línea base = 90,7544952 ms.**

La medición describe el escenario revisado. No demuestra que PostgreSQL sea la causa del resultado, no cuantifica degradación frente a una base pequeña y no representa literalmente el caso original de 10.000 tareas por usuario.
