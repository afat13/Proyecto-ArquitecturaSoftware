# Matrices de fallos y resiliencia — completar tras las mediciones

**Estado:** estructura de análisis, no diagnóstico empírico. No afirmar que un fallo ocurrió sin evidencia.

## Matriz de fallos

| ID | Componente / frontera | Estímulo | Tipo (observado / amenaza) | Evidencia cruda y SHA | Impacto funcional | Explicación alternativa / incertidumbre |
|---|---|---|---|---|---|---|
| PENDIENTE | | | | | | |

## Matriz de resiliencia

| Fallo ID | Táctica candidata | Mecanismo y beneficio buscado | Costo/complejidad | Riesgo secundario | Experimento para comprobar |
|---|---|---|---|---|---|
| PENDIENTE | | | | | |

Se pueden comparar timeouts, retries con backoff, circuit breaker, bulkheads, caché, degradación o balanceo **solo si** existe estímulo concreto que justifique la comparación. No confundir pruebas de carga con pruebas de inyección de fallos. Para una amenaza no observada, explicar qué evidencia falta.

Referencias: [preregistro](../../experimentos/crecimiento-m6/00-preregistro.md) · [resultados](../../experimentos/crecimiento-m6/README.md).
