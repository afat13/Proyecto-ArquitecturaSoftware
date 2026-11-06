# Módulo 6 — Evidencia de fitness function (verde–rojo–verde)

**Proyecto:** Aprende a Aprender  
**Versión de referencia:** `68355bf` (`main`)  
**Fecha de ejecución:** 6 de noviembre de 2026  
**Clase probada:** `com.example.aprendeaprender.api.TaskPaginationFitnessTest`  
**Regla protegida:** la lectura paginada de tareas debe respetar el aislamiento entre usuarios, un tamaño máximo de 100, los parámetros permitidos y el orden determinista de los resultados para datos sin modificaciones concurrentes.

## Procedimiento y resultados

Se ejecutó la prueba de integración del backend con Gradle 8.10.2 y Java 21 dentro de un contenedor Docker. La prueba utilizó PostgreSQL local y comprobó las respuestas HTTP mediante MockMvc con usuarios de prueba.

| Etapa | Código | Resultado observado | Interpretación |
| --- | --- | --- | --- |
| Verde inicial | Código original del commit `68355bf` | `BUILD SUCCESSFUL`; ejecución de la clase de prueba | La regla estaba protegida antes de introducir el cambio |
| Rojo | En un worktree temporal se sustituyó `size > 100` por `size > 1000` en `TaskController.java` | JUnit: 4 tests, 1 failure, 0 errors; `limitaElTamanoYRechazaParametrosInvalidos()` esperaba HTTP 400 y recibió 200 | La prueba detectó una violación deliberada del límite de paginación |
| Verde final | Se restauró `TaskController.java` y se repitió la prueba | `BUILD SUCCESSFUL` (1 min 49 s); JUnit: 4 tests, 0 failures, 0 errors, 0 skipped; salida 0 | La regla volvió a cumplirse tras revertir el cambio |

No se creó una rama para introducir el fallo. Se utilizó un worktree separado, con HEAD desacoplado, para evitar introducir una vulnerabilidad intencional en la rama `main`.

## Archivos de evidencia local

- `docs/modulo-7/evidencias/fitness-verde-inicial.log`: registro del verde inicial.
- `docs/modulo-7/evidencias/fitness-rojo.log`: salida parcial del intento rojo. **No es suficiente por sí sola**, pues PowerShell interrumpió la captura al procesar una advertencia de Java.
- `docs/modulo-7/evidencias/fitness-rojo-junit.xml`: **pendiente de comprobar que se copió antes de ejecutar el verde final**; su contenido esperado es el fallo 400/200 identificado directamente en JUnit.
- `docs/modulo-7/evidencias/fitness-verde-final.log`: salida de la ejecución final con `BUILD SUCCESSFUL`.
- `docs/modulo-7/evidencias/fitness-verde-final-junit.xml`: JUnit reportado con cuatro pruebas aprobadas.

El mensaje `OpenJDK ... Sharing is only supported ...` no fue la causa de la falla funcional. La falla relevante de la fase roja se registró en JUnit. No debemos interpretar el código de salida `-1` de PowerShell como el resultado de la fitness function.

## Alcance y limitaciones

La prueba verifica el contrato HTTP de paginación y el aislamiento entre usuarios en el entorno de integración, pero las identidades autenticadas se simulan mediante MockMvc: no sustituye una prueba de inicio de sesión Bearer de extremo a extremo. La secuencia local se considera realizada. Todavía falta verificar una ejecución satisfactoria del job de backend en GitHub Actions para el commit correspondiente y confirmar que todos los archivos de evidencia roja quedaron guardados.

## Trazabilidad

- Decisión arquitectónica: `docs/adr/ADR-007-distribucion-resiliencia-modulo6.md`
- Especificación de la fitness function: `docs/modulo-6/fitness-function.md`
- Código probado: `backend/src/test/java/com/example/aprendeaprender/api/TaskPaginationFitnessTest.java`
- Código modificado temporalmente: `backend/src/main/java/com/example/aprendeaprender/api/controller/TaskController.java`
- Workflow existente: `.github/workflows/ci.yml`
