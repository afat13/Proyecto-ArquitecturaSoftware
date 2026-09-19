# 05 — Checklist de la entrega

La idea de este archivo es revisar lo que realmente se puede mostrar, no marcar cosas por intención.

## Repositorio

- [ ] rama o commit final identificado;
- [ ] README reproducible;
- [ ] CI en verde;
- [ ] sin secretos reales;
- [ ] contribución de cada integrante si la rúbrica la exige.

## Arquitectura

- [ ] contexto y límites claros;
- [ ] stakeholders y drivers documentados;
- [ ] atributos de calidad priorizados;
- [ ] C4 Nivel 1, 2 y 3;
- [ ] cada caja del C4 tiene un archivo real;
- [ ] cada flecha importante tiene evidencia;
- [ ] tabla de trazado actualizada;
- [ ] registro de elementos corregidos o eliminados;
- [ ] Walking Skeleton de una operación real.

## Experimento

- [ ] hipótesis original sin reescribir;
- [ ] cambio de semilla explicado;
- [ ] 5.000 usuarios y 5.000.000 de tareas verificados;
- [ ] 30 VU con cuentas distintas;
- [ ] cuatro corridas guardadas;
- [ ] corrida 1 tratada como calentamiento;
- [ ] línea base con mediana del p95 2–4;
- [ ] commit medido registrado.

## Defensa

Los tres tenemos que poder abrir sin perder tiempo:

- `ApiService.kt`;
- `ApiClient.kt`;
- `TokenAuthenticationFilter.java`;
- `AuthService.java`;
- `TaskController.java`;
- `resultado.json`.

Y debemos poder explicar el recorrido completo de `GET /api/tasks`.
