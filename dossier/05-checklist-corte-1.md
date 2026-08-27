# Checklist operativo del Corte 1

Utilizar este documento antes de la entrega. No marcar un punto basÃ¡ndose solo en intenciÃ³n; comprobar la evidencia en GitHub.

## Repositorio

- [ ] La rama/commit entregado es identificable.
- [ ] README permite reproducir backend, base y Android.
- [ ] No hay secretos reales versionados.
- [ ] GitHub Actions del commit final estÃ¡ en verde.
- [ ] Existe contribuciÃ³n identificable de cada integrante.
- [ ] Existe al menos un PR propio de cada integrante y se conserva su trazabilidad.

## Arquitectura

- [ ] Contexto y stakeholders estÃ¡n documentados.
- [ ] Drivers y restricciones estÃ¡n documentados.
- [ ] Atributos de calidad estÃ¡n priorizados y justifican tensiones.
- [ ] Existe un escenario de calidad cuantitativo.
- [ ] C4 contexto, contenedores y componentes describen el sistema as-is.
- [ ] Los elementos C4 se pueden rastrear hacia cÃ³digo real.

## Experimento

- [ ] La hipÃ³tesis aparece en Git antes de la mediciÃ³n.
- [ ] No se reescribiÃ³ la hipÃ³tesis despuÃ©s de obtener resultados.
- [ ] La modificaciÃ³n de la semilla estÃ¡ reconocida y no se presenta como el diseÃ±o original.
- [ ] La semilla es reproducible y su volumen fue verificado.
- [ ] La operaciÃ³n evaluada es concreta: `GET /api/tasks`.
- [ ] El instrumento valida cÃ³digo HTTP y contenido.
- [ ] Se registrÃ³ contexto del equipo y commit medido.
- [ ] Se conservan los cuatro JSON y logs crudos.
- [ ] Corrida 1 estÃ¡ excluida como calentamiento.
- [ ] Corridas 2â€“4 son comparables y se usa la mediana del p95.
- [ ] La lÃ­nea base estÃ¡ documentada sin atribuir causas no medidas.

## Defensa

- [ ] Los tres integrantes pueden explicar Android -> API -> PostgreSQL.
- [ ] Los tres pueden explicar por quÃ© Android no accede directamente a PostgreSQL.
- [ ] Los tres conocen operaciÃ³n, semilla, carga y p95.
- [ ] Los tres pueden explicar la diferencia entre hipÃ³tesis original y diseÃ±o finalmente medido.
- [ ] Cada integrante puede seÃ±alar su PR y sus archivos.
