# ADR-007 — Distribución, costo y resiliencia en M6

**Proyecto:** Aprende a Aprender  
**Módulo:** 6 — Crecimiento, resiliencia y costos  
**Estado:** Decisión confirmada por el equipo  
**Fecha:** 23 de octubre de 2026  
**Correspondencia:** «ADR 4» del sílabo, conservando la numeración del repositorio.

## Integrantes del equipo

- Andrés Ardila
- Valentina Silva Paez
- Thomas Huerfano

## 1. Contexto

Aprende a Aprender utiliza un backend desarrollado con Spring Boot y PostgreSQL. Los módulos de Tareas y Materias funcionan dentro de la misma API y comparten operaciones que pueden resolverse mediante transacciones locales.

Durante las pruebas de crecimiento del módulo 6 se evaluó el comportamiento del sistema con 30, 50, 100, 150 y 200 usuarios virtuales.

El p95 global aumentó de 144,66 ms a 1.287,58 ms, mientras que la cantidad de consultas procesadas por segundo permaneció aproximadamente entre 238 y 250.

No se presentaron errores funcionales, pero los resultados evidenciaron una degradación del tiempo de respuesta.

El problema se estudió posteriormente en el módulo de Tareas, donde el endpoint `GET /api/tasks` devolvía 1.000 tareas por usuario en una sola solicitud.

## 2. Alternativas consideradas

### Alternativa A: Mantener la arquitectura actual y optimizar Tareas

Conservar Spring Boot y PostgreSQL, aplicando mejoras progresivas como paginación, filtros y optimización de consultas.

Esta alternativa permite reducir el procesamiento innecesario sin aumentar significativamente la complejidad de la infraestructura.

### Alternativa B: Escalar horizontalmente Spring Boot

Ejecutar varias instancias de la API detrás de un balanceador.

Podría aumentar la capacidad si el procesamiento de Spring Boot fuera el principal factor limitante, pero requeriría infraestructura adicional y no necesariamente resolvería problemas de PostgreSQL.

### Alternativa C: Extraer Tareas como microservicio

Separar el módulo de Tareas para permitir despliegues y escalado independientes.

Su principal desventaja sería introducir mayor complejidad operativa, comunicaciones entre servicios y mecanismos adicionales para mantener la consistencia de los datos.

## 3. Evidencia experimental

Se implementó un endpoint paginado adicional y se realizaron pruebas comparativas con 30 usuarios virtuales durante 60 segundos.

Se ejecutaron cuatro corridas por modalidad, utilizando la primera como calentamiento.

| Métrica | Consulta original | Consulta paginada |
|---|---:|---:|
| Registros por respuesta | 1.000 | 50 |
| Mediana del p95 de GET | 86,68 ms | 15,93 ms |
| Consultas por segundo | 391,55 | 3.086,81 |
| Errores HTTP | 0 | 0 |

La consulta paginada presentó una reducción aproximada del **81,6 % en la latencia p95 por solicitud**.

Estos resultados favorecen el uso de paginación cuando la aplicación necesita consultar únicamente una parte de las tareas.

Sin embargo, la comparación no demuestra que recuperar las 1.000 tareas mediante 20 solicitudes sea más eficiente. Tampoco identifica de manera definitiva el principal cuello de botella del sistema.

## 4. Decisión arquitectónica

**El equipo decidió adoptar la alternativa A: mantener la arquitectura actual y optimizar progresivamente el módulo de Tareas.**

La decisión se fundamenta en que las pruebas realizadas muestran una mejora considerable del tiempo de respuesta al reducir la cantidad de registros por solicitud, sin necesidad de introducir microservicios.

En este momento no existe evidencia suficiente para justificar el costo adicional de distribuir el módulo de Tareas.

Por ello, el equipo prioriza mejorar las consultas, implementar mecanismos de paginación y evaluar nuevas optimizaciones antes de modificar la distribución del backend.

## 5. Mini-comité 2 — Objeciones y respuestas

**Objeción 1: ¿La paginación realmente resuelve el problema?**

La paginación disminuye el tiempo de respuesta por solicitud, pero todavía no demuestra que se haya eliminado el cuello de botella. Se necesitan nuevas pruebas para evaluar otros escenarios y niveles de concurrencia.

**Objeción 2: ¿Qué sucede si el estudiante necesita todas sus tareas?**

Se propone que Android solicite únicamente los datos necesarios para cada pantalla, utilizando filtros por fecha, materia y estado. Cuando se requieran más tareas, se podrán consultar páginas adicionales.

**Objeción 3: ¿Mantener un backend único limita la escalabilidad?**

Puede limitar el escalado independiente de módulos. Sin embargo, el equipo considera que introducir servicios distribuidos antes de identificar las limitaciones reales aumentaría la complejidad sin una justificación experimental suficiente.

**Objeción 4: ¿Qué sucede si las optimizaciones no son suficientes?**

El equipo realizará nuevas mediciones para identificar el recurso limitante y reconsiderará alternativas como optimización de PostgreSQL, escalado horizontal o separación de servicios.

## 6. Consecuencias de la decisión

**Consecuencias positivas**

- Se mantiene una infraestructura menos compleja.
- Se conservan las transacciones locales entre Tareas y Materias.
- Las optimizaciones pueden implementarse y comprobarse progresivamente.
- Se evita asumir costos de distribución que todavía no están justificados.

**Consecuencias negativas y riesgos**

- Spring Boot continúa siendo una unidad de despliegue compartida.
- La paginación todavía requiere adaptación en Android.
- Es necesario manejar correctamente los cambios de datos entre páginas.
- La mejora aún no se ha evaluado con cargas superiores a 30 usuarios virtuales.

## 7. Condiciones para revisar la decisión

La decisión será reconsiderada cuando nuevas mediciones demuestren que las optimizaciones existentes no permiten satisfacer los requisitos de rendimiento, cuando un módulo necesite escalar de manera independiente o cuando las condiciones operativas justifiquen la distribución de servicios.

Las nuevas alternativas deberán compararse experimentalmente antes de su adopción.

## 8. Veredicto del mini-comité

**Veredicto: confirmada la alternativa A.**

El equipo acuerda conservar la arquitectura actual de Spring Boot y PostgreSQL y continuar optimizando el módulo de Tareas.

La paginación constituye la primera optimización evaluada y demuestra una reducción significativa de la latencia por solicitud bajo las condiciones de prueba.

No se considera justificada una migración inmediata a microservicios.

La decisión queda sujeta a revisión cuando se disponga de nueva evidencia técnica que demuestre la necesidad de modificar la distribución del sistema.

## 9. Fitness function

**Estado: pendiente de selección e implementación.**

Como siguiente actividad, el equipo deberá definir una propiedad arquitectónica verificable que respalde la decisión tomada.

Una posibilidad es comprobar automáticamente que el endpoint paginado conserve el aislamiento de datos por usuario, respete los límites de página y mantenga un orden determinista.

La propiedad definitiva deberá implementarse mediante pruebas automatizadas y verificarse en CI.

## 10. Referencias de evidencia

- [Preregistro](../../experimentos/crecimiento-m6/00-preregistro.md)
- [Resultados de crecimiento](../../experimentos/crecimiento-m6/01-analisis-20261023.md)
- [Análisis de paginación](../../experimentos/crecimiento-m6/03-resultados-paginacion.md)
- [Evidencia depurada de paginación](../../experimentos/crecimiento-m6/comparacion-paginacion/)
- [Comparación de costos](../modulo-6/distribucion-costos.md)
- [Matrices de fallos y resiliencia](../modulo-6/matrices.md)
- [Trazabilidad](../modulo-6/trazabilidad.md)
