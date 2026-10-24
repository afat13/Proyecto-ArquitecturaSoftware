# Trazabilidad hacia el Corte 2

| Eslabón | Fuente / estado |
|---|---|
| Línea base original | [Resultados y SHA histórico](../experimento/05-resultado-linea-base.md) — disponible |
| Comparación estilos M4 y diseño modular | [M4](../modulo-4/README.md) — revisión documental, actas pendientes |
| Frontera y contrato M5 | [M5](../modulo-5/README.md) — disponible |
| Reconciliación de preguntas | [Coherencia Spike 1](../modulo-5/coherencia-spike.md) — propuesta documental |
| Preregistro Spike 1 | [00-preregistro](../../experimentos/spike-01/00-preregistro.md) — existente; pendiente ratificación |
| SHA de preregistro aprobado | PENDIENTE |
| Rama de spike y SHA medido | PENDIENTE |
| Salida cruda Spike 1 | PENDIENTE |
| Veredicto y ADR M5 | [ADR-006](../adr/ADR-006-integracion-tareas-modulo5.md) — provisional |
| Preregistro crecimiento M6 | [Preregistro](../../experimentos/crecimiento-m6/00-preregistro.md) — commit `007defa5e6b1de8672516d6607125335b927b8e0`, previo a la medición |
| Rama / SHA de prueba M6 | `main`, commit medido `815c5df5a84692aa9120ce208bff76f5d58bc15b` |
| Resultados de escalones | [Archivos de las 20 corridas](../../experimentos/crecimiento-m6/resultados/20261023T215759Z-815c5df5/) (84 archivos, incluidos JSON, logs y contexto) y [análisis](../../experimentos/crecimiento-m6/01-analisis-20261023.md). Los 20 JSON publicados fueron depurados de `setup_data.tokens`. SHA medido: `815c5df5a84692aa9120ce208bff76f5d58bc15b` |
| Matrices de fallos y resiliencia | [Matrices](matrices.md) — degradación observada; causas no determinadas |
| Comparativa de distribución/costo | [Comparación de alternativas](distribucion-costos.md) — costos relativos documentados; veredicto de alternativa A en ADR-007 |
| ADR M6 + mini-comité | [ADR-007](../adr/ADR-007-distribucion-resiliencia-modulo6.md) — alternativa A confirmada por el equipo; veredicto del mini-comité documentado |
| Fitness definida, verde-rojo-verde | [Fitness candidata](fitness-function.md) — sin implementar ni validar |
| Defensa individual | PENDIENTE: cada participante debe demostrar hechos frente a inferencias y la revisión bajo nuevas restricciones |

**Regla:** no confundir «hay archivo» con «el entregable está verificado». Cada afirmación experimental debe tener SHA, condiciones, datos crudos y evaluación de validez.

## Control de publicación de la evidencia M6

- El preregistro quedó en el commit [007defa](https://github.com/afat13/Proyecto-ArquitecturaSoftware/commit/007defa5e6b1de8672516d6607125335b927b8e0), antes de ejecutar.
- La ejecución reportó el SHA `815c5df5a84692aa9120ce208bff76f5d58bc15b`, distinto de los commits posteriores usados para documentar y depurar.
- Los 84 archivos del experimento están en [`resultados/20261023T215759Z-815c5df5/`](../../experimentos/crecimiento-m6/resultados/20261023T215759Z-815c5df5/).
- Se inspeccionaron los 20 JSON publicados y se retiró el campo `setup_data.tokens` de todos. El resto de las métricas se mantuvo en las versiones publicadas.
- **Importante:** antes de esa corrección se publicaron tokens en commits previos. La modificación no elimina el historial; se deben revocar las sesiones de prueba y, si se requiere eliminar la exposición del historial, seguir el procedimiento de limpieza del repositorio coordinado con el equipo. No se declara ese control terminado.
- El veredicto del equipo (alternativa A) quedó registrado en ADR-007; la definición final y verificación de la fitness function siguen pendientes.

## Comparación posterior: paginación de Tareas

- [Instrumento](../../experimentos/crecimiento-m6/comparar-paginacion.js) y [protocolo](../../experimentos/crecimiento-m6/02-comparacion-paginacion.md) en `main`.
- [Análisis de ocho corridas](../../experimentos/crecimiento-m6/03-resultados-paginacion.md): 30 VUs, 60 segundos; mediana de p95 exclusiva de GET de 86,68 ms (1.000 tareas) y 15,92 ms (50 tareas), sin fallos funcionales registrados.
- **Publicado y verificado en `main`:** [8 JSON depurados y 6 logs](../../experimentos/crecimiento-m6/comparacion-paginacion/). Se confirmó ausencia de `setup_data.tokens` en los JSON actuales; faltan los dos logs de calentamiento (corrida 1).
- Se conserva la diferencia entre pruebas de una solicitud y recuperación total de las 1.000 tareas. Android aún utiliza el endpoint anterior.
