# Atributos de calidad priorizados

## Objetivo

Registrar los atributos de calidad que condicionan las decisiones arquitectÃ³nicas del sistema Aprende a Aprender durante el Corte 1.

## Matriz de priorizaciÃ³n

| Atributo | Prioridad | Motivo | Evidencia o mecanismo actual |
| --- | --- | --- | --- |
| Rendimiento | Alta | La consulta de tareas es una operaciÃ³n frecuente y debe responder de forma predecible bajo concurrencia. | Experimento reproducible sobre `GET /api/tasks`, k6 y PostgreSQL. |
| Seguridad | Alta | El sistema maneja cuentas, contraseÃ±as, sesiones y datos acadÃ©micos. | BCrypt, token Bearer aleatorio, hash SHA-256 del token almacenado y Spring Security. |
| Modificabilidad | Media-Alta | Android, API y persistencia deben poder evolucionar sin acoplar la UI directamente a PostgreSQL. | SeparaciÃ³n Android -> API REST -> PostgreSQL, Retrofit y repositorios. |
| Disponibilidad | Media | La aplicaciÃ³n depende de la API para persistencia remota; una caÃ­da impide sincronizar y consultar datos remotos. | Healthcheck de API y PostgreSQL, Docker Compose y CI. |
| Observabilidad | Media | Para reproducir y explicar resultados deben conservarse contexto, logs y mÃ©tricas. | Actuator, logs de k6, JSON crudos y `contexto.json`. |

## Tensiones arquitectÃ³nicas

### Rendimiento vs. simplicidad

Devolver el listado completo de tareas simplifica el cliente, pero respuestas grandes pueden incrementar consulta, serializaciÃ³n, memoria y transferencia. La lÃ­nea base permite observar el comportamiento antes de proponer optimizaciones.

### Seguridad vs. comodidad

Exigir autenticaciÃ³n para los recursos agrega trabajo a cada solicitud, pero evita exponer datos de otros usuarios. La separaciÃ³n por `user_id` y el token Bearer forman parte del contrato de seguridad.

### Modificabilidad vs. nÃºmero de componentes

Separar Android, API y PostgreSQL agrega despliegue y configuraciÃ³n, pero evita credenciales de base de datos en el cliente y permite modificar persistencia o reglas del backend sin acoplarlas a la UI.

## Atributo principal del escenario

Para el escenario cuantitativo del Corte 1 se prioriza **rendimiento**, observado mediante el p95 del tiempo HTTP de `GET /api/tasks` bajo carga concurrente.
