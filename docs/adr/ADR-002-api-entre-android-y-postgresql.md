# ADR-002 — API REST entre Android y PostgreSQL

**Estado:** Aceptada  
**Tipo:** retrospectivo

## Contexto

La aplicación Android necesita registrar usuarios, consultar materias, tareas, perfil y sincronizar información.

Conectar Android directamente a PostgreSQL dejaría credenciales y reglas de acceso en el cliente, además de mezclar la interfaz móvil con detalles de persistencia.

## Decisión

La comunicación principal queda así:

Android -> API REST Spring Boot -> PostgreSQL

Android consume el backend con Retrofit y OkHttp. El backend valida la identidad, aplica las reglas necesarias y realiza las consultas a PostgreSQL.

No se permite que la aplicación móvil abra conexiones directas a la base de datos.

## Alternativas consideradas

### Acceso directo desde Android

Reduce una capa, pero obliga a exponer la base de datos y hace más difícil controlar autenticación, permisos y cambios del esquema.

### API propia

Agrega un servicio que tenemos que ejecutar y mantener, pero deja una frontera clara entre el cliente y la persistencia.

## Consecuencias

- La aplicación depende de que la API esté disponible.
- Podemos cambiar consultas o estructura interna del backend sin poner SQL dentro de Android.
- La autenticación y la autorización se resuelven del lado servidor.
- Para teléfono físico se debe configurar una dirección alcanzable de la API.
- El recorrido completo tiene más saltos que una conexión directa, pero es una separación que necesitamos.

## Evidencia actual

- app/src/main/java/com/example/aprendeaprender/data/repository/
- cliente HTTP de Android
- backend/src/main/java/com/example/aprendeaprender/api/
- README.md
- Walking Skeleton documentado para GET /api/tasks
