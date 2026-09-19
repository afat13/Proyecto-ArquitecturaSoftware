# 06 — Guion para la defensa

No es para leerlo tal cual. Es para saber qué abre y qué explica cada uno.

## Persona 1 — contexto y contenedores

Mostrar:

- contexto del sistema;
- C4 Nivel 1;
- C4 Nivel 2;
- `docker-compose.yml`.

Explicar:

> El estudiante usa Android. Android habla con una API Spring Boot y la API usa PostgreSQL. UTADEO está afuera del sistema y Gemma corre localmente en Android.

Preguntas que debe responder:

- ¿por qué Android no entra directo a PostgreSQL?;
- ¿dónde está configurada la base?;
- ¿qué papel tiene UTADEO?;
- ¿dónde corre Gemma?

## Persona 2 — componentes y trazabilidad

Mostrar:

- C4 Nivel 3;
- tabla de trazado;
- `SecurityConfig.java`;
- `TokenAuthenticationFilter.java`;
- `TaskController.java`.

Explicar el flujo:

```text
ApiService.getTasks()
 -> ApiClient
 -> TokenAuthenticationFilter
 -> AuthService
 -> TaskController.list()
 -> JdbcClient
 -> PostgreSQL
```

La parte importante es poder abrir el archivo de cada salto.

## Persona 3 — experimento

Mostrar:

- hipótesis original;
- protocolo;
- resultado de línea base;
- `resultado.json`.

Explicar:

> La hipótesis inicial hablaba de 10.000 tareas para una cuenta. Antes de medir cambiamos la semilla a 5.000 usuarios con 1.000 tareas cada uno. Por eso no decimos que la hipótesis original quedó comprobada. La línea base del escenario que sí medimos fue 90,7544952 ms.

## Cosas que no debemos decir

- “PostgreSQL hizo que quedara rápido”.
- “La hipótesis de 10.000 tareas quedó confirmada”.
- “Tenemos repository en el backend para tareas” cuando `TaskController` usa `JdbcClient` directamente.
- “OpenRouter hace parte del flujo actual” sin mostrar dónde se llama.
- “Administrador es un actor implementado” sin poder mostrar el caso de uso.
