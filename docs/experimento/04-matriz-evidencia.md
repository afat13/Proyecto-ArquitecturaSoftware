# Matriz de evidencia del Corte 1

Esta matriz relaciona los principales criterios del corte con evidencia concreta del repositorio. Debe actualizarse si cambia la ubicación de un artefacto.

| Criterio | Evidencia | Estado |
| --- | --- | --- |
| Repositorio y ejecución reproducible | `README.md`, `docker-compose.yml`, `.env.example` | Cumple |
| Persistencia PostgreSQL | `backend/src/main/resources/db/migration/` | Cumple |
| Pruebas y CI | `.github/workflows/ci.yml` | Cumple si el último workflow del commit entregado está verde |
| Contexto y stakeholders | `dossier/01-contexto-sistema.md`, `dossier/02-stakeholders-drivers.md` | Cumple |
| Registro crítico de riesgos/sugerencias de IA (M1) | `dossier/07-registro-critico-ia-riesgos.md` | Cumple |
| Hipótesis previa a la medición | `docs/experimento/01-hipotesis-inicial.md` | Cumple; conservar historial sin reescritura |
| Semilla reproducible | `experimentos/consulta-tareas/seed.sql`, `verificar-semilla.sql` | Cumple |
| Protocolo | `docs/experimento/03-protocolo-medicion.md` | Cumple |
| Instrumentación | `experimentos/consulta-tareas/carga.js`, `ejecutar_experimento.py` | Cumple |
| Evidencia cruda | `experimentos/consulta-tareas/resultados/` | Cumple |
| Línea base | `docs/experimento/05-resultado-linea-base.md`, `resultado.json` | Cumple |
| Comparación con hipótesis | `docs/experimento/06-comparacion-hipotesis.md` | Cumple con la limitación documentada por cambio de semilla |
| Atributos y escenario de calidad | `dossier/03-atributos-calidad.md`, `dossier/04-escenario-calidad.md` | Cumple |
| Registro de escenarios sugeridos por IA y reformulación (M2) | `dossier/08-registro-ia-escenarios-calidad.md` | Cumple |
| C4 as-is | `docs/c4/01-contexto.puml`, `docs/c4/02-contenedores.puml`, `docs/c4/03-componentes.puml` | Cumple |
| Trazabilidad C4 hacia código | `docs/c4/04-trazabilidad-codigo.md` | Cumple |
| Contribución individual | PR #1, PR #2 y PR #3 e historial Git | Cumple; conservar trazabilidad |
| Defensa | `dossier/06-guion-defensa-3-personas.md` | Preparación |

## Evidencia experimental principal

- operación: `GET /api/tasks`;
- semilla medida: 5.000 usuarios, 5 materias por usuario, 1.000 tareas por usuario;
- total: 5.000.000 tareas;
- carga: 30 VUs, una identidad distinta por VU;
- 4 corridas de 60 s;
- corrida 1 descartada como calentamiento;
- línea base: mediana del p95 de corridas 2–4 = aproximadamente 90,75 ms.

Los valores anteriores deben rastrearse hacia los archivos crudos y no sustituirlos.
