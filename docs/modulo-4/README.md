# Módulo 4: decisiones y diseño del proyecto

En este módulo revisamos cómo está organizado Aprende a Aprender y qué sentido tendría cambiar su arquitectura. La aplicación Android consume una API de Spring Boot y esta trabaja con PostgreSQL. Esa es la estructura que tenemos hoy; no vamos a presentar una arquitectura distinta como si ya estuviera implementada.

## Qué opciones revisamos

| Opción | Qué ganamos | Qué nos cuesta |
| --- | --- | --- |
| Mantener la API y ordenar mejor los módulos internos | Seguimos con despliegue sencillo y transacciones locales | Tenemos que controlar el acoplamiento entre módulos |
| Separar Tareas y Materias en servicios | Cada servicio podría evolucionar y escalar por separado | Introducimos llamadas por red, fallos parciales y más trabajo de operación |
| Acceso directo de Android a PostgreSQL | Evitamos una capa intermedia | Comprometemos seguridad y acoplamos el cliente a la base de datos |

Por ahora el código funciona como una aplicación backend única. En `TaskController.create`, la validación de la materia y la inserción de la tarea se resuelven con SQL local. Si separáramos los dos módulos en servicios, tendríamos que rediseñar esa operación y explicar cómo manejar fallos a través de la red. La línea base de rendimiento que tenemos mide consultas, no demuestra que separar servicios mejore algo.

## Cómo están divididas las responsabilidades

Android concentra interfaz, navegación y acceso a la API. En el backend encontramos autenticación, perfil, materias, tareas y retos, organizados en controladores y clases de apoyo. PostgreSQL guarda los datos y Flyway mantiene el esquema. Gemma se ejecuta localmente en Android.

El detalle y las rutas del código están en la [trazabilidad C4](../c4/04-trazabilidad-codigo.md). Para la separación lógica entre Tareas y Materias dejamos el [análisis de frontera](../integracion/contrato-frontera-tareas-materias.md).

## Lo que falta para dar por cerrado M4

Los [ADR históricos](../adr/README.md) explican decisiones que ya estaban en el proyecto. Nos sirven como referencia, pero no prueban por sí solos que el mini-comité de M4 haya revisado dos decisiones nuevas.

Todavía necesitamos guardar la propuesta real que produjo la IA, nuestra crítica respaldada por el código y el acta del mini-comité. En esa acta deben constar asistentes, objeciones, cambios y veredicto de las dos decisiones. No hay acta verificada en esta rama, así que no lo marcamos como terminado.
