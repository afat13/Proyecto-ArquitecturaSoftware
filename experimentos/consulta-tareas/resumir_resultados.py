#!/usr/bin/env python3
"""Resume las corridas de k6 sin alterar los archivos crudos."""

from __future__ import annotations

import json
import statistics
from pathlib import Path

BASE = Path(__file__).resolve().parent
RESULTS = BASE / "resultados"


def metric_values(data: dict, name: str) -> dict:
    metric = data.get("metrics", {}).get(name, {})
    return metric.get("values", {}) if isinstance(metric, dict) else {}


def read_run(number: int) -> dict:
    path = RESULTS / f"corrida-{number}.json"
    data = json.loads(path.read_text(encoding="utf-8"))

    duration = metric_values(data, "http_req_duration{operacion:consulta_tareas}")
    if not duration:
        duration = metric_values(data, "http_req_duration")

    failed = metric_values(data, "http_req_failed")
    requests = metric_values(data, "http_reqs")
    checks = metric_values(data, "checks")

    return {
        "corrida": number,
        "calentamiento": number == 1,
        "p95_ms": duration.get("p(95)"),
        "mediana_ms": duration.get("med"),
        "promedio_ms": duration.get("avg"),
        "solicitudes": requests.get("count"),
        "solicitudes_por_segundo": requests.get("rate"),
        "tasa_fallos_http": failed.get("rate"),
        "checks_exitosos": checks.get("passes"),
        "checks_fallidos": checks.get("fails"),
        "archivo_crudo": path.name,
    }


def main() -> None:
    runs = [read_run(i) for i in range(1, 5)]
    valid = runs[1:]

    if any(run["p95_ms"] is None for run in valid):
        raise RuntimeError("No fue posible encontrar p95 en todas las corridas válidas")

    p95_values = [float(run["p95_ms"]) for run in valid]
    baseline = statistics.median(p95_values)

    summary = {
        "criterio": "mediana del p95 de las corridas 2, 3 y 4",
        "corrida_1_descartada_por": "calentamiento preregistrado",
        "corridas": runs,
        "p95_corridas_validas_ms": p95_values,
        "linea_base_p95_ms": baseline,
        "advertencia": "Este resumen solo es válido si las corridas no presentan errores de instrumentación o de respuesta.",
    }

    (RESULTS / "resultado.json").write_text(
        json.dumps(summary, ensure_ascii=False, indent=2),
        encoding="utf-8",
    )

    print(json.dumps(summary, ensure_ascii=False, indent=2))


if __name__ == "__main__":
    main()
