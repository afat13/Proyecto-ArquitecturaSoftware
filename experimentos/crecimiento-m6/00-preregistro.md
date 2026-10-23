# Preregistro propuesto — experimento de crecimiento M6

**Estado: BORRADOR, NO SELLADO COMO HIPÓTESIS DEFINITIVA, NO EJECUTADO.** Ratificar este protocolo **después** de cerrar M5; crear un commit identificable anterior al primer ensayo de M6. No editar criterios tras ver resultados.

## Pregunta

¿Cómo cambian la latencia, los fallos y el throughput de `GET /api/tasks` al aumentar la concurrencia, y en qué punto dejan de ser interpretables los datos por saturación del generador?

## Diseño propuesto a ratificar

- Escalones: 30 (puente con línea base), 50, 100, 150, 200 VUs.
- Por escalón: una corrida de calentamiento y **tres corridas medidas**, 60 s cada una.
- Cambia solo el número de VUs. Mantener código del servicio, versión y configuración de PostgreSQL/Docker, semilla, host, CPU/memoria disponible, generador k6, script, duración y autenticación. Si no se logra, documentar e invalidar comparación directa.
- Métricas por corrida: `http_req_duration` p50/p95 (para etiqueta de consulta), solicitudes/s, `http_req_failed`, checks funcionales, códigos HTTP, CPU/RAM de backend, PostgreSQL y generador, y signos de saturación del generador.
- Exportar resumen JSON, logs completos y contexto con SHA exacto (servidor y script), fecha/hora, hardware, recursos de contenedores, versión Java/PostgreSQL/k6, dataset e instrumentación.
- Generador potencialmente saturado: CPU sostenida cercana a límite, memoria/paginación, `dropped_iterations`/incapacidad de sostener carga, o evidencia de cuello de botella local. No inferir automáticamente que el backend se degrada cuando el generador falla.
- Invalidar corrida con login incorrecto, datos sin semilla requerida, checks funcionales fallidos por preparación, cambio de configuración, uso no comparable del host o instrumentación ausente para el análisis declarado.
- No alterar índices, queries ni arquitectura durante la curva de crecimiento. Una táctica de resiliencia se ensaya **después** en otro protocolo, manteniendo control.
- Resultados negativos y escalones imposibles deben registrarse.

## Apuesta previa del componente limitante

**Pendiente de hipótesis del equipo, previo a medición.** No se anticipa como hallazgo que PostgreSQL o Spring Boot falle primero. Registrar componente esperado, escalón específico, mecanismo, razón y regla de falsación en commit antes de experimentar, tal como pide la guía del curso.

## Comparabilidad

La [línea base histórica](../../docs/experimento/05-resultado-linea-base.md) mide 30 VUs, cuatro corridas de 60 s, semilla de cinco millones de tareas y p95 mediano de 90,7544952 ms. No es una corrida de este protocolo, ni se debe comparar sin verificar máquina/configuración.

## Clasificación final

Solo luego de ejecutar, clasificar cada escalón como válido / inválido y separar hechos observados de atribuciones causales.
