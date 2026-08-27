# Matriz de evidencia del Corte 1

Esta matriz relaciona los principales criterios del corte con evidencia concreta del repositorio. Debe actualizarse si cambia la ubicaciÃ³n de un artefacto.

| Criterio | Evidencia | Estado |
| --- | --- | --- |
| Repositorio y ejecuciÃ³n reproducible | `README.md`, `docker-compose.yml`, `.env.example` | Cumple |
| Persistencia PostgreSQL | `backend/src/main/resources/db/migration/` | Cumple |
| Pruebas y CI | `.github/workflows/ci.yml` | Cumple si el Ãºltimo workflow del commit entregado estÃ¡ verde |
| Contexto y stakeholders | `dossier/01-contexto-sistema.md`, `dossier/02-stakeholders-drivers.md` | Cumple |
| HipÃ³tesis previa a la mediciÃ³n | `docs/experimento/01-hipotesis-inicial.md` | Cumple; conservar historial sin reescritura |
| Semilla reproducible | `experimentos/consulta-tareas/seed.sql`, `verificar-semilla.sql` | Cumple |
| Protocolo | `docs/experimento/03-protocolo-medicion.md` | Cumple |
| InstrumentaciÃ³n | `experimentos/consulta-tareas/carga.js`, `ejecutar_experimento.py` | Cumple |
| Evidencia cruda | `experimentos/consulta-tareas/resultados/` | Cumple |
| LÃ­nea base | `docs/experimento/05-resultado-linea-base.md`, `resultado.json` | Cumple |
| ComparaciÃ³n con hipÃ³tesis | `docs/experimento/06-comparacion-hipotesis.md` | Cumple con la limitaciÃ³n documentada por cambio de semilla |
| Atributos y escenario de calidad | `dossier/03-atributos-calidad.md`, `dossier/04-escenario-calidad.md` | Depende del PR correspondiente |
| C4 as-is | `docs/c4/` | Depende del PR correspondiente |
| ContribuciÃ³n individual | historial Git y PR de cada integrante | Debe comprobarse al cierre |
| Defensa | `dossier/06-guion-defensa-3-personas.md` | PreparaciÃ³n |

## Evidencia experimental principal

- operaciÃ³n: `GET /api/tasks`;
- semilla medida: 5.000 usuarios, 5 materias por usuario, 1.000 tareas por usuario;
- total: 5.000.000 tareas;
- carga: 30 VUs, una identidad distinta por VU;
- 4 corridas de 60 s;
- corrida 1 descartada como calentamiento;
- lÃ­nea base: mediana del p95 de corridas 2â€“4 = aproximadamente 90,75 ms.

Los valores anteriores deben rastrearse hacia los archivos crudos y no sustituirlos.
