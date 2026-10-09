# Contrato de integración existente — consulta de tareas

**Proveedor:** módulo de tareas en backend Spring Boot. **Consumidor:** `TaskRepository.kt` (Android/Retrofit). **Operación implementada:** `GET /api/tasks`. **Seguridad:** sesión Bearer; la identidad autenticada determina el filtro de registros. **Transporte:** HTTP/JSON síncrono.

## Respuesta
`200 OK`: arreglo JSON de tareas del usuario. Sus campos se derivan de `TaskController.TaskResponse` y `ApiModels.kt`: identificador, materia asociada, nombre de materia, título, descripción, fecha de entrega, prioridad, estado y creación.

Ejemplo **ilustrativo** (no captura de una respuesta real):
```json
[{"id":"00000000-0000-0000-0000-000000000001","subjectId":"00000000-0000-0000-0000-000000000002","subjectName":"Cálculo","title":"Taller 1","description":"","dueAt":null,"priority":"MEDIA","status":"PENDIENTE","createdAt":"2026-10-09T10:00:00Z"}]
```

## Errores y límites
- Sin autenticación o token inválido: la seguridad del backend impide consultar recursos privados; verificar con prueba de integración el código HTTP y cuerpo exactos.
- Error de servidor o almacenamiento: fallo HTTP; el consumidor debe manejar error/reintento sin crear tareas duplicadas.
- No se permiten lecturas de tareas de terceros por pasar un `userId` arbitrario: el backend deriva el usuario de `Authentication`.
- Esta consulta no crea tareas ni modifica estado.

## Versionamiento
La ruta **actual** es `/api/tasks`, sin prefijo `/v1`. No afirmar que hay versionamiento implementado. Una futura versión `/api/v1/tasks` será un cambio planeado; deberá coexistir temporalmente con la ruta anterior o llevar un plan de migración del cliente.

## Trazabilidad
`app/src/main/java/com/example/aprendeaprender/data/api/ApiService.kt` → `data/repository/TaskRepository.kt` → `backend/src/main/java/com/example/aprendeaprender/api/controller/TaskController.java` → PostgreSQL. Contrato documentado del sistema actual; no es una API nueva.
