# 08 — Cómo aterrizamos los escenarios

Al principio varias ideas estaban escritas demasiado generales. Las fuimos convirtiendo en cosas que se pudieran medir o comprobar.

| Idea inicial | Problema | Cómo quedó |
| --- | --- | --- |
| “la consulta debe ser rápida” | no decía carga ni métrica | `GET /api/tasks`, 30 VU y p95 |
| “un usuario con 10.000 tareas” | no fue la semilla final | se conserva como hipótesis histórica |
| “los datos deben ser seguros” | no decía qué se protege | autenticación + filtro por `user_id` |
| “el sistema debe escalar” | mezcla varias cosas | se limitó al escenario que realmente medimos |
| “hay que paginar” | era una solución antes de medir | queda como posible cambio futuro |

El escenario que sí se ejecutó terminó con 5.000 usuarios, 1.000 tareas por usuario y 5.000.000 de tareas. La línea base fue **90,7544952 ms** de p95 usando la mediana de las corridas 2–4.
