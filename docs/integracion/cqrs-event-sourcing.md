# Análisis de CQRS y Event Sourcing — Aprende a Aprender

## CQRS
Separaría responsabilidades y modelos de lectura y escritura de tareas/retos si sus requisitos divergieran. Actualmente `TaskController` y `ChallengeController` consultan y escriben sobre PostgreSQL dentro del backend Spring Boot, sin prueba de necesidad de proyecciones independientes.

**Beneficios hipotéticos:** vistas especializadas, escalado diferenciado si se verificara presión; **costos:** sincronización, consistencia eventual, datos duplicados, más pruebas y observabilidad. **Decisión propuesta:** no introducir CQRS completo. Optimizar consultas y límites de respuesta antes de multiplicar modelos. La línea base histórica `GET /api/tasks` es p95=90,75 ms bajo condiciones documentadas; no representa el nuevo spike.

## Event Sourcing
Conservaría la historia completa como fuente primaria y reconstruiría estado de tareas/retos con eventos. Actualmente las tablas relacionales persisten estado. No hay requisito comprobado de reconstrucción integral. **Costos:** versionamiento de eventos, retención, snapshots, recuperación, privacidad y migraciones. **Decisión propuesta:** no adoptar en esta etapa.

## Consistencia
- **Propiedad de materia al crear tarea:** requiere comprobación del estado actual.
- **Acceso del usuario a tareas y retos:** verificación inmediata de identidad y propiedad.
- **Notificaciones locales y vistas derivadas:** pueden admitir retrasos si existe una especificación que lo tolere.
- **Preguntas propuestas por IA:** su generación es local, pero el guardado remoto exige validación de materia propia.

## Revisión
Evaluar estos patrones solo si un escenario funcional y medidas repetibles muestran que el modelo actual no puede satisfacer lectura, escritura o auditoría bajo restricciones definidas.
