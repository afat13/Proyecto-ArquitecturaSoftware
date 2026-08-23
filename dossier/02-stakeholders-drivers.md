# Stakeholders y drivers arquitectónicos

## Stakeholders

| Stakeholder | Interés en el sistema | Expectativas o preocupaciones |
| --- | --- | --- |
| Estudiante usuario | Organizar su trabajo académico y estudiar | Información disponible, tiempos de respuesta razonables, sesiones seguras y sincronización confiable |
| Equipo de desarrollo | Construir, probar y evolucionar el sistema | Código mantenible, arquitectura comprensible, entorno reproducible y errores detectables antes de integrar cambios |
| Docente evaluador | Evaluar decisiones y evidencia arquitectónica | Trazabilidad entre código, hipótesis, mediciones, Git, C4 y afirmaciones del equipo |
| UTADEO / sistema académico externo | Fuente externa de información | Es una dependencia fuera del control del equipo; cambios o fallos afectan la sincronización |
| Operador del backend | Ejecutar API y PostgreSQL | Configuración mediante variables, healthchecks, persistencia y diagnóstico sencillo |

## Drivers arquitectónicos preliminares

### 1. Persistencia relacional y trazable

Materias, tareas, usuarios, sesiones y retos tienen relaciones explícitas y requieren integridad referencial. Esto conduce a PostgreSQL, claves foráneas, restricciones e índices administrados mediante migraciones Flyway.

### 2. Separación cliente–base de datos

La aplicación Android no debe conocer credenciales de PostgreSQL ni ejecutar SQL directamente. Por ello la arquitectura introduce una API REST como límite entre cliente y persistencia.

### 3. Seguridad de autenticación

Las contraseñas no se almacenan en texto plano. Se utiliza BCrypt. Las sesiones usan tokens aleatorios enviados como Bearer; la base de datos conserva su hash SHA-256 y una fecha de expiración en lugar del token original.

La recuperación de contraseña y la verificación de correo se reconocen como necesidades futuras, pero no forman parte del alcance implementado para esta primera versión de autenticación.

### 4. Rendimiento ante crecimiento de datos

Un estudiante puede acumular materias y tareas. La operación `GET /api/tasks` se selecciona como escenario inicial para obtener una línea base reproducible con 10.000 tareas y carga concurrente. En esta etapa todavía no se presupone cuál componente será responsable de un posible deterioro.

### 5. Reproducibilidad

El equipo debe poder reconstruir el entorno, ejecutar pruebas y repetir el experimento. Docker Compose, Flyway, una semilla determinista, scripts de carga y GitHub Actions soportan este driver.

### 6. Modificabilidad

La persistencia anterior estaba acoplada a servicios Firebase dentro del cliente Android. La nueva separación mediante `ApiService`, repositorios Android, controladores de backend y esquema versionado busca que cambios de almacenamiento no obliguen a propagar detalles de PostgreSQL hasta la interfaz.

### 7. Continuidad de capacidades locales

Gemma continúa ejecutándose localmente. La migración de persistencia no debe convertir la generación de preguntas en una dependencia del backend ni eliminar el uso de WorkManager.

### 8. Interoperabilidad con UTADEO

La aplicación necesita transformar información proveniente de UTADEO a sus entidades internas. La sincronización externa se mantiene en Android, mientras la persistencia del resultado pasa por la API y PostgreSQL.

## Priorización preliminar de drivers

1. Correctitud e integridad de datos.
2. Seguridad de autenticación y separación de credenciales.
3. Reproducibilidad de ejecución y pruebas.
4. Rendimiento de operaciones de consulta bajo crecimiento de datos.
5. Modificabilidad de persistencia e integraciones.
6. Interoperabilidad con UTADEO.
7. Continuidad del procesamiento local de IA.

Esta priorización es preliminar. La matriz formal de atributos de calidad y su justificación se mantiene en un documento separado para permitir que tenga autoría y trazabilidad propias.

## Tensiones arquitectónicas observables

- Una autenticación más robusta agrega complejidad operativa frente a una solución mínima de sesión.
- Obtener todos los datos de una sola vez puede simplificar el cliente, pero puede perjudicar rendimiento cuando el volumen aumenta.
- Mantener Gemma local mejora independencia frente a un servicio de IA remoto, pero depende de memoria, almacenamiento y capacidad del dispositivo.
- Sincronizar frecuentemente con UTADEO mejora actualidad de datos, pero aumenta uso de red y dependencia de un sistema externo.
- Agregar índices puede mejorar lecturas, pero tiene costo en escritura y almacenamiento; no se adoptarán optimizaciones únicamente por intuición antes de medir.

## Preguntas abiertas

- ¿El listado completo de 10.000 tareas es una carga representativa suficiente para revelar el fenómeno esperado?
- ¿El escenario requiere paginación u otra estrategia después de observar la línea base?
- ¿Qué nivel de disponibilidad real se requerirá cuando el backend deje de ejecutarse solo en un entorno académico/local?
- ¿Qué mecanismo se utilizará posteriormente para verificación de correo y recuperación de contraseña?

Estas preguntas se mantienen abiertas para evitar convertir supuestos en decisiones prematuras.
