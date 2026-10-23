# Propuesta de IA y revisión del equipo — Módulo 6

**Solicitud hecha a la IA en esta revisión:** «Propón una estrategia para convertir Aprende a Aprender, que hoy tiene una API Spring Boot y PostgreSQL, en una arquitectura de microservicios. Indica ventajas, riesgos, costos y cuándo sería razonable migrar».

## Propuesta generada por IA

Separar progresivamente la autenticación, las materias y las tareas en servicios desplegables. Los clientes consumirían un punto de entrada API; cada servicio se encargaría de sus datos y se comunicaría mediante contratos explícitos. Se podría añadir balanceo de carga y observabilidad. Una migración gradual reduciría el riesgo de moverlo todo al mismo tiempo.

## Revisión con lo que sabemos del proyecto

La parte aprovechable es la idea de tener responsabilidades claras y migrar por etapas en vez de reemplazar el backend completo. Sin embargo, la propuesta no presenta evidencia de que Aprende a Aprender necesite procesos independientes. Hoy la operación de creación de tareas valida la materia en una consulta SQL dentro del backend. Si Tareas y Materias se separaran, esa comprobación introduciría llamadas de red o nuevas reglas de consistencia.

Tampoco tenemos todavía mediciones de crecimiento de M6, fallos observados o costos reales de operar varios despliegues. Por eso sería prematuro convertir la propuesta en una decisión. El experimento de `GET /api/tasks` debe mostrar primero dónde se degrada la consulta. Aun así, un problema en GET no demostraría automáticamente que separar microservicios sea la solución.

## Comprobación posterior

Cuando estén los resultados, revisar afirmación por afirmación: beneficio justificado, costo agregado, evidencia de M6 y alternativa más sencilla. El veredicto arquitectónico corresponde al equipo y se registrará en [ADR-007](../adr/ADR-007-distribucion-resiliencia-modulo6.md).

**Estado:** se guardó una propuesta generada de IA para discusión; la auditoría contra los nuevos resultados permanece pendiente.
