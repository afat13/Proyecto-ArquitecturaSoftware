#!/usr/bin/env python3
"""Resume las corridas de k6 sin alterar los archivos crudos.

Tolera variantes del JSON de resumen entre versiones de k6 y conserva
los archivos originales como evidencia del experimento.
"""

from __future__ import annotations

import json
import re
import statistics
from pathlib import Path
from typing import Any

BASE = Path(__file__).resolve().parent
RESULTS = BASE / "resultados"


def metric_values(data: dict, name: str) -> dict:
    """Obtiene los valores de una métrica en distintas variantes de k6."""
    metric = data.get("metrics", {}).get(name, {})
    if not isinstance(metric, dict):
        return {}

    values = metric.get("values")
    if isinstance(values, dict):
        return values

    # Compatibilidad defensiva con exportaciones donde los agregados
    # aparecen directamente dentro del objeto de la métrica.
    return metric


def find_metric_values(data: dict, base_name: str, required_tag: str | None = None) -> dict:
    metrics = data.get("metrics", {})
    if not isinstance(metrics, dict):
        return {}

    if required_tag:
        exact = f"{base_name}{{{required_tag}}}"
        values = metric_values(data, exact)
        if values:
            return values

        # El orden o los tags adicionales pueden variar entre versiones.
        for metric_name in metrics:
            if metric_name.startswith(base_name + "{") and required_tag in metric_name:
                values = metric_values(data, metric_name)
                if values:
                    return values

    return metric_values(data, base_name)


def percentile(values: dict, percentile_value: int = 95) -> float | None:
    """Encuentra p95 aunque k6 cambie levemente el texto de la clave."""
    preferred = [
        f"p({percentile_value})",
        f"p({percentile_value}.0)",
        f"p({percentile_value}.00)",
        f"p{percentile_value}",
    ]
    for key in preferred:
        value = values.get(key)
        if isinstance(value, (int, float)):
            return float(value)

    pattern = re.compile(r"^p\(?(\d+(?:\.\d+)?)\)?$", re.IGNORECASE)
    for key, value in values.items():
        if not isinstance(value, (int, float)):
            continue
        match = pattern.match(str(key).strip())
        if match and abs(float(match.group(1)) - percentile_value) < 1e-9:
            return float(value)

    return None


def numeric(values: dict, *keys: str) -> float | int | None:
    for key in keys:
        value = values.get(key)
        if isinstance(value, (int, float)):
            return value
    return None


def read_run(number: int) -> dict:
    path = RESULTS / f"corrida-{number}.json"
    if not path.exists():
        raise RuntimeError(f"No existe el archivo requerido: {path}")

    data = json.loads(path.read_text(encoding="utf-8"))

    duration = find_metric_values(
        data,
        "http_req_duration",
        "operacion:consulta_tareas",
    )
    failed = find_metric_values(data, "http_req_failed")
    requests = find_metric_values(data, "http_reqs")
    checks = find_metric_values(data, "checks")

    p95 = percentile(duration, 95)

    return {
        "corrida": number,
        "calentamiento": number == 1,
        "p95_ms": p95,
        "mediana_ms": numeric(duration, "med", "median"),
        "promedio_ms": numeric(duration, "avg", "average", "mean"),
        "solicitudes": numeric(requests, "count"),
        "solicitudes_por_segundo": numeric(requests, "rate"),
        "tasa_fallos_http": numeric(failed, "rate"),
        "checks_exitosos": numeric(checks, "passes"),
        "checks_fallidos": numeric(checks, "fails"),
        "archivo_crudo": path.name,
        "claves_duracion_detectadas": sorted(str(k) for k in duration.keys()),
    }


def main() -> None:
    runs = [read_run(i) for i in range(1, 5)]
    valid = runs[1:]

    missing = [run["corrida"] for run in valid if run["p95_ms"] is None]
    if missing:
        details = {
            run["corrida"]: run["claves_duracion_detectadas"]
            for run in valid
            if run["corrida"] in missing
        }
        raise RuntimeError(
            "No fue posible encontrar p95 en las corridas "
            f"{missing}. Claves detectadas: {json.dumps(details, ensure_ascii=False)}"
        )

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
