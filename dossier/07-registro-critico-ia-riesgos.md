# 07 — Registro de sugerencias de IA

Usamos IA como apoyo, pero varias sugerencias no coincidían con el proyecto. Este archivo deja claro qué se mantuvo y qué no.

| Sugerencia | Resultado | Motivo |
| --- | --- | --- |
| Android podría conectarse directo a PostgreSQL | Falso | el cliente usa Retrofit y la API |
| muchas tareas pueden afectar la consulta | Válido | por eso medimos `GET /api/tasks` |
| PostgreSQL “va a ser lento” | Muy general | sin carga y métrica no se puede comprobar |
| deberíamos usar Kubernetes | Fuera de alcance | no hay una necesidad medida para este corte |
| Firebase sigue siendo la persistencia principal | Falso | la arquitectura actual usa API + PostgreSQL |
| una sola corrida basta | Falso | usamos cuatro y dejamos la primera como calentamiento |
| OpenRouter debe aparecer porque existe el archivo | No alcanza | un archivo no demuestra que el flujo actual pase por ahí |

La regla que estamos usando es la misma del C4: existencia de código no significa automáticamente que sea un elemento arquitectónico activo. Hay que poder seguir la relación.
