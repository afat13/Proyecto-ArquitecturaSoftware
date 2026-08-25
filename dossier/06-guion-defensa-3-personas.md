# Guion de defensa para tres integrantes

No memorizar palabra por palabra. Utilizarlo como distribuciÃ³n de temas y practicar respuestas con el repositorio abierto.

## AndrÃ©s â€” sistema y migraciÃ³n tÃ©cnica

1. Presentar el objetivo de Aprende a Aprender.
2. Explicar la arquitectura actual: Android -> API REST Spring Boot -> PostgreSQL.
3. Explicar por quÃ© se retirÃ³ Firebase de la persistencia principal.
4. Mostrar Docker Compose, Flyway, seguridad y CI.
5. SeÃ±alar que Android no conoce credenciales PostgreSQL.

Preguntas que debe poder responder:

- Â¿CÃ³mo se autentica una solicitud?
- Â¿DÃ³nde se almacena la contraseÃ±a?
- Â¿QuÃ© ocurre si cambia PostgreSQL?
- Â¿QuÃ© comprueba CI?

## Thomas â€” atributos de calidad y C4

1. Mostrar atributos priorizados.
2. Explicar las tensiones rendimiento/seguridad/modificabilidad.
3. Recorrer C4: contexto, contenedores y componentes.
4. Relacionar cada elemento con cÃ³digo concreto.
5. Explicar el escenario de rendimiento y su umbral.

Preguntas que debe poder responder:

- Â¿Por quÃ© rendimiento es importante?
- Â¿CuÃ¡l es el estÃ­mulo, ambiente, respuesta y medida?
- Â¿QuÃ© diferencia hay entre contexto, contenedor y componente?

## Valentina â€” experimento y evidencia

1. Mostrar la matriz de evidencia.
2. Explicar la semilla medida: 5.000 usuarios x 1.000 tareas.
3. Explicar los 30 VUs con identidades independientes.
4. Mostrar las cuatro corridas y por quÃ© la primera no entra en el resultado.
5. Mostrar la lÃ­nea base aproximada de 90,75 ms.
6. Explicar que la hipÃ³tesis inicial se conserva y que el cambio de semilla se documenta como una limitaciÃ³n metodolÃ³gica.

Preguntas que debe poder responder:

- Â¿QuÃ© significa p95?
- Â¿Por quÃ© usar varias corridas?
- Â¿Por quÃ© descartar la primera?
- Â¿QuÃ© demuestra el experimento y quÃ© NO demuestra?

## Cierre compartido

La evidencia permite afirmar cÃ³mo se comportÃ³ `GET /api/tasks` bajo las condiciones registradas. No permite atribuir automÃ¡ticamente el resultado a PostgreSQL, Ã­ndices, JVM, red o cachÃ© sin una mediciÃ³n adicional que aÃ­sle esas causas.
