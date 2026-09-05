# 05 — Checklist de verificación

## Arquitectura

- [ ] El commit/branch entregado está identificado.
- [ ] README reproduce backend, DB y Android.
- [ ] C4 contexto, contenedores y componentes representan el sistema as-is.
- [ ] Cada caja tiene ancla en `docs/c4/04-trazabilidad-codigo.md`.
- [ ] No se inventa una capa repository en el backend para tareas/materias/perfil/retos.
- [ ] OpenRouter no se dibuja como flujo activo sin evidencia de llamada.
- [ ] Administrador no se presenta como actor implementado sin caso verificable.

## Experimento

- [ ] La hipótesis original permanece intacta.
- [ ] El cambio de semilla está reconocido.
- [ ] Se verifican 5.000 usuarios, 25.000 materias y 5.000.000 de tareas.
- [ ] Se usan 30 VU con cuentas distintas.
- [ ] Corrida 1 es calentamiento.
- [ ] La línea base usa mediana de p95 2–4.
- [ ] Se conserva el commit medido.
- [ ] No se atribuyen causas no aisladas.

## Defensa

- [ ] Los tres pueden recorrer Android → API → PostgreSQL.
- [ ] Pueden abrir `ApiService.kt`, `TokenAuthenticationFilter.java` y `TaskController.java`.
- [ ] Pueden mostrar `resultado.json`.
- [ ] Pueden explicar por qué 90,75 ms no confirma literalmente la hipótesis de 10.000 tareas.
