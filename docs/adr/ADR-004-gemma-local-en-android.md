# ADR-004 — Gemma se ejecuta localmente en Android

**Estado:** Aceptada  
**Tipo:** retrospectivo

## Contexto

Los retos de estudio necesitan generar preguntas con IA.

Durante el proyecto se contempló usar un servicio remoto para esta capacidad, pero eso deja la funcionalidad atada a conexión, disponibilidad externa y límites de uso.

La implementación actual ya incluye descarga y manejo de un modelo Gemma dentro de la aplicación.

## Decisión

Gemma queda como una capacidad local de Android, no como un servicio remoto representado aparte en la arquitectura activa.

El modelo se descarga al almacenamiento interno de la aplicación y la ejecución se realiza en el dispositivo con la integración local correspondiente.

La descarga se maneja con WorkManager y contempla reanudación, archivo temporal y validaciones del modelo.

## Alternativas consideradas

### Servicio remoto de IA

Tiene la ventaja de no cargar el dispositivo con el modelo y permite usar modelos más grandes, pero depende de red, disponibilidad y límites del proveedor.

### Gemma local

Reduce esa dependencia durante la generación y mantiene la capacidad dentro del dispositivo, a cambio de consumo de almacenamiento, memoria y tiempo de descarga.

## Consecuencias

- El modelo ocupa espacio en el teléfono.
- El rendimiento depende del dispositivo.
- Hay que manejar descarga, reintentos y validación del archivo.
- Una vez disponible el modelo, la generación no depende de una llamada de negocio a un proveedor remoto.
- El modelo local debe aparecer dentro del límite de Android en el C4 y no como un sistema externo independiente.

## Evidencia actual

- GemmaModelConstants.kt
- GemmaModelDownloadWorker.kt
- código bajo app/src/main/java/com/example/aprendeaprender/data/ai/
- descripción de arquitectura en README.md
