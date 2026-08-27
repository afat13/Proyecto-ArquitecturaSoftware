# Matriz de evidencia del Corte 1

La siguiente matriz muestra dónde se encuentra la evidencia de cada criterio evaluado en el repositorio.

| Criterio | Evidencia | Estado |
| --- | --- | --- |
| Repositorio y ejecución reproducible | `README.md`, `docker-compose.yml`, `.env.example` | Cumple |
| Persistencia PostgreSQL | `backend/src/main/resources/db/migration/` | Cumple |
| Pruebas e integración continua | `.github/workflows/ci.yml` y ejecuciones de GitHub Actions | Cumple |
| Contexto y stakeholders | `dossier/01-contexto-sistema.md`, `dossier/02-stakeholders-drivers.md` | Cumple |
| Registro crítico de riesgos y sugerencias de IA (M1) | `dossier/07-registro-critico-ia-riesgos.md` | Cumple |
| Hipótesis previa a la medición | `docs/experimento/01-hipotesis-inicial.md` | Cumple |
| Semilla reproducible | `experimentos/consulta-tareas/seed.sql`, `experimentos/consulta-tareas/verificar-semilla.sql` | Cumple |
| Protocolo de medición | `docs/experimento/03-protocolo-medicion.md` | Cumple |
| Instrumentación | `experimentos/consulta-tareas/carga.js`, `experimentos/consulta-tareas/ejecutar_experimento.py` | Cumple |
| Evidencia cruda | `experimentos/consulta-tareas/resultados/` | Cumple |
| Línea base | `docs/experimento/05-resultado-linea-base.md`, `experimentos/consulta-tareas/resultados/resultado.json` | Cumple |
| Comparación con la hipótesis | `docs/experimento/06-comparacion-hipotesis.md` | Cumple |
| Atributos y escenario de calidad | `dossier/03-atributos-calidad.md`, `dossier/04-escenario-calidad.md` | Cumple |
| Registro de escenarios sugeridos por IA y reformulación (M2) | `dossier/08-registro-ia-escenarios-calidad.md` | Cumple |
| C4 as-is | `docs/c4/01-contexto.puml`, `docs/c4/02-contenedores.puml`, `docs/c4/03-componentes.puml` | Cumple |
| Trazabilidad C4 hacia código | `docs/c4/04-trazabilidad-codigo.md` | Cumple |
| Contribución individual | PR #1, PR #2, PR #3 e historial Git | Cumple |
| Preparación de la defensa | `dossier/06-guion-defensa-3-personas.md` | Cumple |

## Evidencia experimental principal

- Operación evaluada: `GET /api/tasks`.
- Semilla medida: 5.000 usuarios, 5 materias por usuario y 1.000 tareas por usuario.
- Volumen total: 25.000 materias y 5.000.000 de tareas.
- Carga: 30 usuarios virtuales, cada uno autenticado con una cuenta distinta.
- Duración: 60 segundos por corrida.
- Corridas ejecutadas: 4.
- Corrida 1: calentamiento, excluida del cálculo final.
- Línea base: mediana del p95 de las corridas 2–4 = aproximadamente **90,75 ms**.

La evidencia primaria de la medición se conserva en `experimentos/consulta-tareas/resultados/`.
