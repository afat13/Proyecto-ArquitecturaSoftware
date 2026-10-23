# Qué estamos comprobando en el Spike 1

Hay dos asuntos distintos que quedaron mezclados en M5. El preregistro y el ADR-006 hablan de `GET /api/tasks` y de su respuesta por REST. El documento de Tareas–Materias, en cambio, trata la validación de una materia al crear una tarea con `POST /api/tasks`.

La medición de 90,75 ms que ya hicimos sirve como línea base de **GET**. No prueba nada sobre **POST** ni sobre separar responsabilidades entre esos módulos.

Antes de ejecutar un spike nuevo hay que definir cuál de esas preguntas queremos responder y guardar esa decisión en un commit. No vamos a reescribir una hipótesis anterior como si siempre hubiera tratado sobre otra cosa. Si el experimento trata de `POST`, necesita protocolo, pruebas y criterios propios. Si trata de `GET`, debe comprobar algo adicional a la línea base que ya tenemos; repetir exactamente la misma carga no aporta una respuesta nueva.

Hasta resolverlo, el [ADR-006](../adr/ADR-006-integracion-tareas-modulo5.md) sigue siendo provisional. La [línea base](../experimento/05-resultado-linea-base.md) se conserva tal como fue medida.
