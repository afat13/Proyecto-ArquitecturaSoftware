# Evaluación de integración: síncrona frente a asíncrona

## Problema concreto
El cliente necesita obtener tareas de su sesión mediante `GET /api/tasks`, y el backend debe devolver solamente las del usuario autenticado. Hoy esto ocurre mediante Retrofit + API Spring Boot + PostgreSQL.

| Criterio | REST síncrono actual | Publicación y consumo de eventos propuestos |
|---|---|---|
| Dependencia temporal | Android espera respuesta | Productor y consumidor pueden desacoplarse en el tiempo |
| Acoplamiento | Ruta HTTP y representación conocidas | Contrato de evento, esquema y canal de entrega |
| Consistencia | Consulta del estado persistido en ese momento | Vista materializada potencialmente desactualizada |
| Disponibilidad | Un fallo de API afecta la consulta | Requiere cola, reintentos y política de duplicados |
| Complejidad | Infraestructura ya presente y medida | Broker, consumidores, observabilidad y manejo de fallas |
| Observabilidad | Petición/respuesta y pruebas k6 existentes | Retrasos, entregas, reintentos, correlación y DLQ adicionales |

## Decisión provisional
Conservar REST síncrono para consultar tareas. Para esta función interactiva no existe evidencia de que un broker reduzca riesgo o complejidad. La línea base histórica de `GET /api/tasks` es p95=90,75 ms, documentada en `docs/experimento/05-resultado-linea-base.md`; **no es una medición del nuevo spike**.

**Reevaluación**: si aparecen requerimientos demostrados de consumidores desacoplados o procesamiento en segundo plano que tolera consistencia eventual.

No se confunde notificación local de Android mediante WorkManager con arquitectura distribuida de eventos.
