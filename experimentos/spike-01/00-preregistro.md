# Spike 1 — Preregistro previo a cualquier implementación

**Estado:** hipótesis definida; nuevo spike **sin ejecutar**. Este documento debe quedar en un commit anterior a cualquier cambio experimental.

## Pregunta
¿Una consulta REST síncrona de tareas con contrato explícito y sin modificar la persistencia mantiene el escenario de calidad ya observado bajo la carga de referencia?

## Hipótesis falsable
En el entorno reproducible existente, `GET /api/tasks` mantendrá la mediana de p95 de tres corridas válidas **por debajo de 150 ms**, sin checks funcionales fallidos.

**Nota:** 150 ms es un umbral experimental propuesto para el spike, **no** un requisito recuperado del sílabo ni una garantía actual del sistema. Debe aprobarse antes de ejecutar.

## Variables y protocolo predefinidos
- Variable principal: mediana de p95 de corridas 2, 3 y 4; primera corrida de calentamiento.
- Variable de corrección: checks de respuesta y aislamiento de datos de la sesión.
- Condiciones constantes: semilla, script k6, máquina/topología, número de VU, duración, ramas/versiones medidas, configuración Docker y PostgreSQL.
- Escenario heredado como base metodológica: 30 VU, 60 segundos por corrida, cuatro corridas; ver `experimentos/consulta-tareas/`.
- Criterio de aceptación: mediana p95 < 150 ms y cero checks funcionales fallidos.
- Refutación: mediana p95 >= 150 ms o al menos un check funcional fallido.
- No tocar: consulta SQL de negocio, índices, semilla, credenciales, seguridad, lógica de retos ni arquitectura del backend.
- Alcance autorizado del spike posterior: instrumentación y verificación mínima del contrato, sin refactorización general.

## Comparación obligatoria
Línea base histórica: 90,75 ms para `GET /api/tasks`; `docs/experimento/05-resultado-linea-base.md`. No sirve como resultado nuevo ni atribuye causalidad. Registrar SHA y condiciones del nuevo experimento para comprobar comparabilidad.

## Resultado
**PENDIENTE**. No hay números, conclusión ni veredicto de este spike.

## Secuencia en Git
1. Commit de este preregistro en rama documental.
2. Abrir `spike/01-contrato-tareas` desde la referencia revisada, solo después del commit 1.
3. Implementar el cambio mínimo; guardar diff, SHA y resultados crudos.
4. Comparar y redactar veredicto; después cerrar ADR 3.
