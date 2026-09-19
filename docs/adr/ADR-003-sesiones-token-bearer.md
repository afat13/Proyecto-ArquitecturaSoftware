# ADR-003 — Sesiones propias con token Bearer

**Estado:** Aceptada  
**Tipo:** retrospectivo

## Contexto

La API necesita identificar al usuario en las operaciones protegidas sin depender de una sesión HTTP tradicional.

Además, tareas y materias deben consultarse usando la identidad autenticada y no un userId enviado libremente por Android.

## Decisión

Al iniciar sesión o registrarse, el backend genera un token aleatorio y lo entrega al cliente.

Android lo envía mediante el encabezado Authorization con esquema Bearer.

El backend no guarda el token original. Guarda su hash SHA-256 en auth_session junto con el usuario y la fecha de expiración.

TokenAuthenticationFilter valida el token con AuthService y coloca el identificador del usuario en el contexto de seguridad de Spring.

Las contraseñas se almacenan con BCrypt.

## Alternativas consideradas

### Sesión HTTP tradicional

No era la opción que mejor encajaba con el cliente móvil y la API sin estado a nivel HTTP.

### JWT

Era posible, pero para este proyecto no necesitábamos meter información firmada dentro del token. Con sesiones almacenadas podemos invalidarlas directamente al cerrar sesión.

### Token aleatorio con sesión en base de datos

Mantiene el mecanismo simple y permite controlar expiración y cierre de sesión desde PostgreSQL.

## Consecuencias

- Cada petición protegida necesita validar la sesión.
- Existe una consulta adicional a base de datos durante la autenticación.
- El logout puede eliminar la sesión inmediatamente.
- Una filtración de la tabla de sesiones no expone directamente los tokens originales.
- La duración de sesión debe configurarse y controlarse.

## Evidencia actual

- SecurityConfig.java
- TokenAuthenticationFilter.java
- AuthService.java
- tabla auth_session
