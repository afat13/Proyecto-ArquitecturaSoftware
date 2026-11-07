# Plan de cierre M7–M8 — contraste con el informe de revisión

**Proyecto:** Aprende a Aprender  
**Revisión:** 6 de noviembre de 2026  
**Referencia:** `main` en commit `80a30a1`; CI [#217](https://github.com/afat13/Proyecto-ArquitecturaSoftware/actions/runs/42318012828) verde.

Este archivo separa lo que el informe externo señala como pendiente de lo que realmente encontramos en el repositorio. El informe reconoce que fue una revisión parcial; por tanto, sus «no confirmados» no son automáticamente trabajos ausentes.

## Evidencias verificadas desde el repositorio

| Entregable | Evidencia accesible | Estado |
| --- | --- | --- |
| CI Android y backend | GitHub Actions #214, #215, #216 y #217: success | Verificado |
| Crecimiento M6 | 20 corridas, scripts, preregistro y análisis en `experimentos/crecimiento-m6` | Documentado con datos crudos |
| Fitness function verde–rojo–verde | `docs/modulo-6/fitness-function-m6-evidencia.md`; XML rojo: 4 tests / 1 fallo; verde: 4/4 | Verificado localmente y test aprobado en CI |
| STRIDE | `docs/modulo-7/01-modelado-amenazas.md` | Documentado; riesgos sin mitigar |
| Despliegue y costos | `docs/modulo-7/02-diseno-despliegue.md` | Documentado |
| Observabilidad y carga | `03-observabilidad.md`, `04-resultados-observabilidad.md`, 16 PNG originales y log k6 | Ejecutado y documentado |
| Riesgos y recuperación API | `05-riesgos-operativos-recuperacion.md` | Interrupción y recuperación manual validadas |
| ADR correspondiente a M7 | [ADR-008 propuesto](../adr/ADR-008-observabilidad-recuperacion-m7.md) | Documento redactado; decisión del equipo pendiente |
| 4+1 | `docs/modulo-7/06-vistas-arquitectonicas-4mas1.md` | Complementario; no sustituye C4 |

## Pendientes efectivos

| Prioridad | Pendiente | Qué prueba lo cerrará |
| --- | --- | --- |
| Alta | Ratificación del ADR-008 | Acta o confirmación del equipo con objeciones, costos y veredicto |
| Alta | Spike 1 del M5 | Preregistro aprobado **antes** de la ejecución, SHA, resultados crudos y veredicto. El antiguo `experimentos/spike-01/00-preregistro.md` indica que no fue ejecutado; no debe hacerse pasar la carga M6 por ese spike |
| Alta | M8 — 7 documentos formales del dossier | Archivos 01–07 con límites de palabras exigidos, referencias y trazabilidad; el resumen `docs/tercer-corte/README.md` no los reemplaza |
| Alta | M8 — Matriz de trade-offs, evaluación y plan de evolución | Matrices que enlacen decisiones con escenarios, evidencia, sacrificios, reversibilidad y umbrales |
| Alta | M8 — Re-medición final | Preregistro de condiciones, 4 corridas como mínimo si se aplica el protocolo habitual (descartar 1 y mediana de 3), SHA, datos originales y comparación válida |
| Alta | M8 — Declaración final del uso de IA | Especificaciones previas, alternativas aceptadas/rechazadas, auditoría y decisión humana comprobable |
| Alta | Defensa individual y grupal | Atribución real de cambios, revisión entre pares y capacidad de ejecutar y explicar evidencias |
| Media | Actualización de referencias heredadas del M6 | Sustituir avisos antiguos «CI/fitness pendientes» por referencias a resultados reales, sin reescribir la historia |
| Media | Completar plan operativo M7 | Añadir señales, umbrales como propuestas —no límites supuestamente aprobados—, procedimiento de alertas, responsables y riesgos residuales |
| Media | Publicación de PostgreSQL / HTTP del laboratorio | Decisión y pruebas de mitigación, sin romper k6 ni los contratos existentes |
| Media | Backup/restauración de PostgreSQL | `pg_dump` y recuperación verificados en instancia aislada |
| Complementaria | 4+1 y dashboards adicionales | Solo si aportan nueva evidencia o lo exige el docente |

## Estructura que exige el Módulo 8

Se propone la carpeta `docs/modulo-8/`, con estos siete documentos (a crear y verificar):

| Nombre | Máximo indicado en el informe |
| --- | --- |
| `01-contexto-drivers.md` | 800 palabras |
| `02-escenarios-calidad.md` | 900 palabras + tablas |
| `03-vistas-c4-trazabilidad.md` | Diagramas + 600 palabras |
| `04-evidencia-ejecutable.md` | 1.000 palabras |
| `05-riesgos-tradeoffs-costos.md` | Matrices + 600 palabras |
| `06-operacion-seguridad.md` | 800 palabras |
| `07-brief-ejecutivo.md` | Una página |

Los ADR y el contrato API se entregan aparte. Las medidas no deben convertirse en resultados de la re-medición final mientras esa re-medición no se haya ejecutado.

## Orden de avance propuesto

1. Ratificar ADR-008 (decisión humana faltante).
2. Consolidar los siete archivos de M8 reutilizando únicamente evidencias existentes.
3. Revisar y conciliar el Spike 1 del M5, sin modificar hipótesis después de resultados.
4. Definir preregistro y condiciones de la re-medición final; ejecutar cuando haya acceso al laboratorio.
5. Preparar contribución individual, declaración de IA y defensa.
