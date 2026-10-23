# Preregistro del experimento de crecimiento — Módulo 6

**Fecha del protocolo:** 23 de octubre de 2026. **Estado:** definido antes de medir M6. Este archivo debe conservarse sin modificar cuando existan resultados; cualquier cambio posterior al diseño se explica en otro documento.

## Pregunta

¿Cómo cambia el rendimiento y la corrección de `GET /api/tasks` cuando aumentamos la cantidad de usuarios virtuales en el mismo backend?

## Qué cambiamos y qué mantenemos

- **Variable independiente:** usuarios virtuales simultáneos: **30, 50, 100, 150 y 200**, siempre en este orden. El nivel de 30 permite contrastar condiciones con la medición de M2.
- **Constantes:** mismo equipo y configuración Docker, backend sin cambios entre niveles, imagen PostgreSQL 16, semilla de 5.000 cuentas, 25.000 materias y 5.000.000 de tareas; exactamente 1.000 tareas por usuario, mismo script k6 y credenciales de prueba, misma duración de **60 segundos**.
- **Repeticiones por nivel:** cuatro corridas: una de calentamiento y tres para el análisis. Se mantienen las cuatro salidas, incluso si una falla.
- **Sin optimizaciones intermedias:** no modificar consultas, índices, pool de conexiones ni código durante el experimento. Si cambia algo, ese nivel no puede compararse directamente con los anteriores.

## Qué registramos

Para cada corrida: SHA del código probado, fecha, hardware, versión de Docker/k6, número de VUs, p50 y p95 de la consulta, throughput, checks funcionales, códigos HTTP, errores y duración. También uso de CPU y memoria del backend, PostgreSQL y del generador cuando sea posible medirlos. Registrar si el generador comparte equipo con los servicios. Conservar los JSON y logs de k6, sin sustituirlos por una tabla.

## Cómo comprobamos que las respuestas sirven

Antes de la carga, verificar que la semilla tiene los volúmenes esperados. Cada VU debe iniciar sesión con su usuario y obtener token válido. En cada GET, comprobar código 200, respuesta JSON y exactamente 1.000 tareas; conservar distribución de códigos HTTP de la consulta. Los fallos de respuesta durante una carga válida **son resultados de degradación**, no se descartan solo porque sean negativos.

## Corridas inválidas

Son inválidas las corridas donde falta el conjunto de datos, falla la preparación/login, se rompe el instrumento, no se puede identificar el commit, cambia la configuración sin registro o falta la información mínima para saber qué se ejecutó. Si el generador se satura o pierde capacidad de sostener la carga, registrar la corrida como **no concluyente para atribuir el límite al backend**; conservar la evidencia y explicar el problema. No atribuir automáticamente cualquier lentitud a PostgreSQL o Spring Boot.

## Cómo analizamos

Reportar p50/p95 y throughput por nivel y la mediana de los p95 de las corridas 2–4 si las tres son válidas. No reemplazar los p95 originales con ese resumen. Comparar contra la [línea base M2](../../docs/experimento/05-resultado-linea-base.md): 30 VUs, 60 s por corrida y p95 mediano de 90,7544952 ms, con commit `0bbff1ca77b2884a6658d690f58564b2aa37da79`. La comparación cuantitativa directa exige revisar diferencias de máquina, código y configuración.

## Lo que todavía no sabemos

No se define de antemano qué componente fallará primero. Primero se miden los escalones y después se construyen las matrices de fallos y resiliencia. El experimento no demuestra por sí solo que se necesiten microservicios.
