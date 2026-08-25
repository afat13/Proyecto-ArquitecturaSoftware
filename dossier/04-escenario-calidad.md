# Escenario principal de calidad: rendimiento

## Atributo

Rendimiento.

## Fuente del estÃ­mulo

Usuarios autenticados de la aplicaciÃ³n Android, representados durante la mediciÃ³n por usuarios virtuales de k6.

## EstÃ­mulo

Treinta usuarios concurrentes solicitan su listado de tareas mediante:

`GET /api/tasks`

Cada usuario virtual utiliza una cuenta distinta.

## Ambiente

API Spring Boot y PostgreSQL 16 ejecutados mediante Docker Compose, con una semilla de 5.000 usuarios, 5 materias por usuario y 1.000 tareas por usuario, para 5.000.000 de tareas totales.

## Artefactos afectados

- filtro de autenticaciÃ³n y sesiÃ³n;
- controlador/repository de tareas del backend;
- pool de conexiones JDBC;
- PostgreSQL y sus Ã­ndices por usuario;
- serializaciÃ³n HTTP/JSON.

## Respuesta esperada

La API debe responder HTTP 200 y devolver Ãºnicamente las 1.000 tareas correspondientes al usuario autenticado, sin mezclar informaciÃ³n entre cuentas.

## Medida cuantitativa

- carga: 30 usuarios virtuales;
- duraciÃ³n: 60 segundos por corrida;
- 4 corridas;
- corrida 1 tratada como calentamiento;
- mÃ©trica principal: p95 de `http_req_duration` para `GET /api/tasks`;
- umbral preregistrado del instrumento: p95 < 2.000 ms;
- resultado de lÃ­nea base documentado en `docs/experimento/05-resultado-linea-base.md`.

## RelaciÃ³n con la hipÃ³tesis

La hipÃ³tesis inicial se conservÃ³ sin modificaciÃ³n. Antes de medir se ampliÃ³ la semilla a un escenario multiusuario; la comparaciÃ³n y la limitaciÃ³n de esa modificaciÃ³n estan documentadas en `docs/experimento/06-comparacion-hipotesis.md`.
