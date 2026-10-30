# Fitness function — Módulo 6

**Decisión protegida:** [ADR-007](../adr/ADR-007-distribucion-resiliencia-modulo6.md): mantener el backend Spring Boot + PostgreSQL y optimizar Tareas sin distribuir prematuramente sus responsabilidades.

## Propiedad arquitectónica

La lectura paginada de Tareas debe conservar el aislamiento entre usuarios, limitar cada respuesta a un máximo de 100 tareas y mantener orden determinista entre páginas para una base de datos sin modificaciones concurrentes.

**Implementación:** [`TaskPaginationFitnessTest`](../../backend/src/test/java/com/example/aprendeaprender/api/TaskPaginationFitnessTest.java), prueba de integración HTTP con MockMvc y PostgreSQL real, datos creados en cada prueba y transacción de prueba revertida al finalizar.

Reglas verificadas:

1. Un usuario A no recibe tareas de B, y viceversa.
2. `size=1` responde un único registro; `size=0`, `size>100`, `page<0` y desbordamientos del desplazamiento se rechazan con HTTP 400.
3. En una base de datos estática, `page=0` y `page=1` con tamaño 1 devuelven tareas diferentes en orden completo (desempate por ID); una página posterior queda vacía.
4. Una petición sin credenciales no puede consultar tareas.

**No garantiza:** consistencia de páginas si se insertan o eliminan tareas entre solicitudes, rendimiento mínimo p95, ni que Android use este endpoint. El campo `mayHaveMore` indica una página llena, no confirma páginas posteriores.

## CI

El job de backend de [`.github/workflows/ci.yml`](../../.github/workflows/ci.yml) ya ejecuta `gradle -p backend test --no-daemon` en JDK 21 con PostgreSQL 16. La clase nueva forma parte de ese comando, por lo que no se necesita un segundo workflow.

Localmente con PostgreSQL levantado y accesible según `DB_URL`:

```powershell
gradle -p backend test --tests "com.example.aprendeaprender.api.TaskPaginationFitnessTest" --no-daemon
```

El resultado en CI debe revisarse en [Actions](https://github.com/afat13/Proyecto-ArquitecturaSoftware/actions/workflows/ci.yml). No afirmar resultado verde sin verificar una ejecución asociada al commit correcto. El pipeline también compila Android: una falla de Android es independiente de estas pruebas de backend.

## Demostración verde–rojo–verde (pendiente de evidencia)

**Protocolo, no resultados observados.** Para evitar dejar un fallo en `main`, realizar en una copia local temporal, sin crear rama:

1. **Verde inicial:** guardar SHA y salida de `gradle -p backend test --tests "com.example.aprendeaprender.api.TaskPaginationFitnessTest" --no-daemon`.
2. **Rojo:** sobre la copia, modificar **solo** la consulta paginada del controlador: quitar temporalmente `WHERE t.user_id = :userId` y la llamada a `.param("userId", userId)` correspondiente. Volver a ejecutar. La prueba `cadaUsuarioSoloVeSusPropiasTareas` debería fallar porque devuelve también las tareas de otro usuario. No subir esta versión.
3. **Verde final:** restaurar el archivo original desde Git y repetir la prueba. Guardar salidas y SHA del código correcto.

Precaución: la consulta original `GET /api/tasks` y las operaciones de creación/sincronización no se modifican para esta prueba; no eliminar filtros de seguridad fuera de la copia temporal. Es necesario documentar los tres resultados reales antes de cerrar M6.

## Evidencias de rendimiento separadas

- [Pruebas de crecimiento](../../experimentos/crecimiento-m6/01-analisis-20261023.md).
- [Comparación de paginación](../../experimentos/crecimiento-m6/03-resultados-paginacion.md): mediana de p95 por GET, 86,68 ms para 1.000 tareas y 15,93 ms para 50 tareas en 30 VUs. No confundir esto con el resultado de la fitness function.
