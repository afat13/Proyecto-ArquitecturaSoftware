# Verificación del informe de revisión — 6 de noviembre de 2026

**Alcance:** contraste del informe recibido con la rama `main` (revisada después del commit `80a30a1`) y el pipeline de CI. Se distinguen comprobaciones técnicas de decisiones pendientes del equipo. No se atribuye al informe una revisión del código posterior a su propia inspección.

## 1. Elementos que el informe marca como no confirmados y ya tienen evidencia

| Elemento del informe | Evidencia encontrada en el repositorio | Dictamen |
| --- | --- | --- |
| CI #215 | [Ejecución #215](https://github.com/afat13/Proyecto-ArquitecturaSoftware/actions/runs/42316095420) — completada con éxito | Confirmado |
| CI #214, #216 y #217 | [#214](https://github.com/afat13/Proyecto-ArquitecturaSoftware/actions/runs/42314951164), [#216](https://github.com/afat13/Proyecto-ArquitecturaSoftware/actions/runs/42317958044), [#217](https://github.com/afat13/Proyecto-ArquitecturaSoftware/actions/runs/42318012828) — todos verdes | Confirmado |
| Verde–rojo–verde | [Evidencia de fitness](../modulo-6/fitness-function-m6-evidencia.md), [JUnit rojo](../modulo-7/evidencias/fitness-rojo-junit.xml) y [JUnit verde](../modulo-7/evidencias/fitness-verde-final-junit.xml) | Ejecutado localmente; no equivale a prueba de mutación en CI |
| Prometheus / Grafana | [Compose de observabilidad](../../docker-compose.observabilidad.yml), [configuración](../../observabilidad/prometheus.yml), [capturas originales](../modulo-7/evidencias/grafana/README.md) | Instalación y funcionamiento comprobados en laboratorio |
| Amenazas M7 | [Matriz STRIDE](../modulo-7/01-modelado-amenazas.md) | Análisis documentado; controles pendientes |
| Despliegue M7 y costos | [Diseño de despliegue](../modulo-7/02-diseno-despliegue.md) | Topología local comprobada; alternativas cualitativas |
| Riesgos operativos | [Riesgos y recuperación](../modulo-7/05-riesgos-operativos-recuperacion.md) | Recuperación manual de API probada; backup y failover no probados |
| Capturas originales | [16 PNG en repositorio](../modulo-7/evidencias/grafana/README.md) | Versionadas |

## 2. Pendientes confirmados

| Entregable | Falta real | Siguiente acción admisible |
| --- | --- | --- |
| Spike 1 del M5 | El preregistro dice `PENDIENTE`; pregunta sobre contrato GET y frontera Tareas–Materias no son la misma | Ratificar pregunta/protocolo antes de ejecutar; no sustituir con resultados M6 |
| ADR 5 / M7 | No existía un registro exclusivo de la decisión operativa | [ADR-008 propuesto](../adr/ADR-008-operacion-seguridad-observabilidad-modulo7.md), pendiente de ratificación |
| Plan de observabilidad | Configuración y métricas comprobadas; faltan umbrales y procedimientos de alerta y reacción formalizados | Elaborar tabla señal–umbral–acción y separar propuesta de alerta habilitada |
| Matriz de riesgos M7 | Ocho riesgos presentes, pero faltan responsables por rol y revisión formal de riesgo residual | Ampliar sin asignar personas ni inventar aceptaciones |
| Dossier M8 | [Resumen tercer corte](README.md) no equivale a los siete documentos exigidos por ficha | Crear los siete artefactos con límites de palabras y referencias |
| Trade-offs / evaluación / evolución M8 | No se verificó un expediente final separado | Consolidar decisiones existentes sin inventar aprobación |
| Re-medición final M8 | No hay protocolo final aplicado con tres corridas y descarte de calentamiento sobre versión final | Preregistro primero; ejecutar cuando se tenga PC, con condiciones comparables |
| Auditoría final de IA | Existen críticas parciales; no un cierre integral de delegación | Conservar especificación, propuestas aceptadas/rechazadas y verificación |
| Contribución individual | No se han revisado todas las revisiones entre pares y autorías | Auditar commits y PR; no asignar aportes por inferencia |
| Defensa | Faltan guion y enlaces de demo estables | Preparar narrativa de evidencia y preguntas nuevas |

## 3. Complementos versus requisitos

Las vistas 4+1 y Grafana apoyan la defensa, pero el informe recuerda que no sustituyen C4 ni el plan de observabilidad. Una gráfica no constituye dato experimental original; k6 mantiene sus JSON/logs. Los resultados de 30 VUs de M7 son una validación instrumentada, no reemplazan la re-medición M8 requerida.

## 4. Riesgos de trazabilidad

Hay documentos antiguos del M6 y del despliegue M7 que continúan describiendo como pendientes labores realizadas después (CI, fitness y Prometheus). No deben utilizarse como estado actual sin una nota de actualización.

Otra cuestión abierta es el uso histórico de tokens en commits de resultados M6. El hecho de eliminar esos campos en la versión actual no prueba que se revocaron los tokens ni que desaparecieron del historial.

## 5. Orden de cierre recomendado

1. Mantener CI verde, corregir enlaces/estados documentales y completar ADR M7 como propuesta verificable.
2. Elaborar el plan de observabilidad operativo con umbrales y responsables por rol; ratificar las decisiones.
3. Consolidar los siete documentos M8, trade-offs, evaluación y evolución.
4. Recuperar/ratificar Spike 1; preregistrar re-medición final y ejecutar según condiciones fijadas.
5. Integrar las evidencias crudas, revisar la declaración de IA y preparar la defensa individual.

**Criterio:** requisito → evidencia → método → resultado → decisión → limitaciones → SHA. Un archivo existente no equivale a una prueba ejecutada.
