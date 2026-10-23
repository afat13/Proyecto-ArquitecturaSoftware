# Matrices de fallos y resiliencia — M6

Basadas en las [20 corridas del 23 de octubre](../../experimentos/crecimiento-m6/01-analisis-20261023.md). Separamos lo que sí vimos de lo que todavía no podemos atribuir a ningún componente.

## Matriz de fallos / degradaciones

| ID | Condición | Qué observamos | Evidencia | Impacto | Qué no sabemos |
|---|---|---|---|---|---|
| F1 | Aumentar concurrencia de 30 a 200 VUs | p95 **agregado** subió de 144,66 a 1.287,58 ms, con GET/s aproximadamente estables (237,97–242,61 entre extremos) | `corridas.json`, JSON individuales y [análisis](../../experimentos/crecimiento-m6/01-analisis-20261023.md) | Respuesta más lenta bajo carga; no hubo errores funcionales | No podemos señalar API, SQL, Docker ni host como causa sin medición durante la carga |
| F2 (riesgo de observación) | Generador y API/DB comparten computador | La captura no mide utilización de k6 ni recursos en forma continua | `contexto.json`, capturas puntuales `docker stats` | Podemos interpretar incorrectamente el límite de rendimiento | Si el generador también alcanzó su capacidad |
| F3 (limitación del instrumento) | Exportación de k6 | No apareció una serie de p95 exclusiva de `operacion:consulta_tareas`; el p95 global también cuenta logins | JSON de k6 y [auditoría](../../experimentos/crecimiento-m6/01-analisis-20261023.md) | La métrica global no permite decir el p95 preciso de GET | Diferencia exacta entre p95 de GET y p95 agregado |

**No se observó caída HTTP funcional:** 321.106 respuestas 200 de GET, 642.212 checks correctos y ninguno fallido. F2 y F3 son riesgos o límites de medición, **no fallos demostrados de producción**.

## Matriz de resiliencia (alternativas a comprobar)

| Caso | Alternativa | Beneficio esperado | Costo y riesgo | Cómo se validaría |
|---|---|---|---|---|
| F1 | Revisar consulta, índices y plan SQL antes de distribuir | Mejor latencia manteniendo despliegue sencillo | Tiempo de perfilado; un índice adicional tiene costo en escrituras | `EXPLAIN ANALYZE`, revisión de métricas y comparación de cargas equivalentes |
| F1 | Control de concurrencia y tiempos máximos de espera | Evitar que solicitudes lentas consuman recursos indefinidamente | Rechazo de solicitudes, límites de servicio y errores visibles | Prueba de estrés controlada; verificar latencia y tasa de errores |
| F1 | Escalado horizontal de API con balanceador | Añadir capacidad en capa API **si** allí está el límite | Instancias, balanceador, conexiones y observabilidad; DB sigue compartida | Medir con igual semilla y más de una réplica, controlando CPU/DB |
| F1 | Caché de lecturas de tareas | Reducir trabajo repetido, si hay lecturas reutilizables | Invalidación, aislamiento entre usuarios, riesgo de datos obsoletos | Medir tasa de aciertos, frescura y latencia bajo consultas repetidas |
| F2 y F3 | Mejorar observabilidad e instrumento antes de elegir táctica | Separar límites del generador y de backend; medir p95 solo de GET | Instrumentación y repetición complementaria | Muestras continuas de recursos y submétricas etiquetadas |

No hay evidencia de fallos remotos que justifique hoy declarar obligatorio un circuit breaker, reintentos o servicios separados. Si se evalúan, primero definir el estímulo específico y los errores que pretenden contener.
