# Coherencia entre contrato, frontera y Spike 1

## Problema detectado

Hay **dos preguntas distintas** en la documentación:
1. El preregistro histórico de [Spike 1](../../experimentos/spike-01/00-preregistro.md) y [ADR-006](../adr/ADR-006-integracion-tareas-modulo5.md) hablan de mantener `GET /api/tasks` REST síncrono bajo una carga determinada.
2. El [contrato Tareas → Materias](../integracion/contrato-frontera-tareas-materias.md) analiza verificación de pertenencia de la materia al crear una tarea con `POST /api/tasks`.

La línea base histórica de 90,75 ms corresponde únicamente a **GET**, por tanto **no valida POST ni el encapsulamiento de la frontera**.

## Separación metodológica propuesta para revisar antes de medir

- **Spike 1 tal como está preregistrado:** verificar contrato y comportamiento de `GET /api/tasks` bajo condiciones comparables. Su umbral propuesto es p95 mediano de tres corridas válidas < 150 ms, cero checks fallidos; requiere ratificación por equipo antes de ejecución.
- **Frontera Tareas–Materias:** queda como análisis de diseño modular sin reclamar verificación experimental de su encapsulamiento. Si se necesita experimentar con esa incertidumbre, preregistrar **otro** spike, con criterios funcionales de pertenencia, concurrencia y atomicidad, sin reutilizar mediciones del GET.

**No se cambia la pregunta del preregistro existente en retrospectiva.** Tampoco se modifica el backend en este commit documental.

## Validación mínima del Spike 1

Comparar con el SHA histórico `0bbff1ca77b2884a6658d690f58564b2aa37da79` únicamente si máquina, volumen de datos, configuración, modo de ejecución y script son comparables. Registrar diferencias de versión actual, SHA de preregistro, SHA medido, datos de `http_req_failed` y checks funcionales. Si no se puede igualar el entorno, informar diferencia e invalidar comparaciones causales. Ningún resultado nuevo puede inferirse del p95 histórico.

## Estado

**Reconciliación documental preparada, decisión final del equipo y ejecución pendientes.** Ver [ADR-006](../adr/ADR-006-integracion-tareas-modulo5.md) y [guía experimental](../../experimentos/spike-01/README.md).
