# Registro crítico de propuestas de IA — Módulo 5

Este documento distingue el **código observado** de las alternativas generadas para comparación. No adjudica a los autores revisiones humanas o mediciones todavía no realizadas.

| Propuesta | Clasificación | Contraste con el repositorio |
| --- | --- | --- |
| TareaCompletada | Candidata | `TaskController.updateStatus` registra estados; no existe publicador de dominio observado. |
| MateriaEliminada | Candidata | `SubjectController.delete` permite eliminar, pero no emite un evento observado. |
| RetoDiarioCompletado | Candidata | `ChallengeController.complete` actualiza estado; no hay broker. |
| Contrato lógico Tareas → Materias | Alternativa para estudio | `TaskController.create` ya consulta `subject` dentro de una transacción. |
| API HTTP interna entre controladores | No justificada hoy | Mismo proceso y base; añadiría acoplamiento a transporte. |
| Kafka/RabbitMQ para validar `subjectId` | Rechazada sin evidencia | Introduciría estado replicado e idempotencia donde hoy existe verificación inmediata. |
| `BotonCompletarTareaPresionado` | Rechazada | Click de interfaz, no hecho consumible del negocio. |
| `RegistroInsertado` | Rechazada | Detalle técnico de persistencia. |
| `CertificadoEmitido` | Rechazada como hecho actual | No hay flujo de certificados acreditado en los controladores revisados. |
| CQRS / Event Sourcing para CRUD corriente | No adoptada | No se demostró necesidad de proyecciones de lectura ni reconstrucción a partir de eventos. |

## Revisión humana pendiente
El equipo debe verificar nombres, contratos, decisiones y mediciones antes de presentarlos como resultados aceptados. El spike y el ADR se cierran con **evidencia propia**, no por recomendación automática.
