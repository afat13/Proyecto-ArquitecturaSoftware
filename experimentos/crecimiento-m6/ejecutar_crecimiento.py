#!/usr/bin/env python3
"""Ejecuta los escalones de M6 sin tocar los resultados de M2."""
import csv
import io
import json
import os
import platform
import statistics
import subprocess
import sys
from datetime import datetime, timezone
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
EXP = Path(__file__).resolve().parent
BASE = ROOT / "experimentos" / "consulta-tareas"
LEVELS = (30, 50, 100, 150, 200)
RUNS = 4
DURATION = "60s"


def command(args, *, input_data=None, check=True):
    return subprocess.run(args, cwd=ROOT, input=input_data, text=True,
                          capture_output=True, check=check)


def out(args):
    p = command(args, check=False)
    return p.stdout.strip() if p.returncode == 0 else "no disponible"


def verify_seed():
    sql = (BASE / "verificar-semilla.sql").read_text(encoding="utf-8")
    data = command(["docker", "compose", "exec", "-T", "db", "psql",
                    "-U", os.getenv("POSTGRES_USER", "aprende"),
                    "-d", os.getenv("POSTGRES_DB", "aprende_aprender"),
                    "-v", "ON_ERROR_STOP=1", "--csv"], input_data=sql).stdout
    rows = list(csv.DictReader(io.StringIO(data)))
    if len(rows) != 1:
        raise RuntimeError("La verificación de datos no produjo una fila")
    row = rows[0]
    expected = {"usuarios": 5000, "correos_unicos": 5000, "materias": 25000,
                "total_tareas": 5000000, "min_tareas_usuario": 1000,
                "max_tareas_usuario": 1000, "usuarios_con_1000_tareas": 5000}
    for key, value in expected.items():
        if int(row[key]) != value:
            raise RuntimeError(f"Semilla incorrecta: {key}={row[key]}, esperado={value}")
    if float(row["promedio_tareas_usuario"]) != 1000:
        raise RuntimeError("Promedio de tareas incorrecto")
    return data


def values(metric):
    return metric.get("values", metric) if isinstance(metric, dict) else {}


def summary(path, level, number, exit_code):
    data = json.loads(path.read_text(encoding="utf-8")) if path.exists() else {}
    metrics = data.get("metrics", {})
    tagged = [k for k in metrics if k.startswith("http_req_duration{") and
              "operacion:consulta_tareas" in k]
    duration = values(metrics.get(tagged[0] if tagged else "http_req_duration", {}))
    checks = values(metrics.get("checks", {}))
    requests = values(metrics.get("http_reqs", {}))
    failures = values(metrics.get("http_req_failed", {}))
    p95 = duration.get("p(95)")
    p50 = duration.get("med", duration.get("p(50)"))
    errors = checks.get("fails")
    # Una corrida con respuestas incorrectas bajo carga es un hallazgo, no se descarta.
    valid = exit_code == 0 and p95 is not None and errors is not None
    return {"vus": level, "corrida": number, "calentamiento": number == 1,
            "p50_ms": p50, "p95_ms": p95, "solicitudes_s": requests.get("rate"),
            "http_failed_rate": failures.get("rate"), "checks_fallidos": errors,
            "salida_k6": exit_code, "instrumentacion_completa": valid,
            "json": path.name}


def main():
    if os.getenv("M6_CONFIRMAR_EJECUCION") != "SI":
        raise SystemExit("Esta prueba requiere M6_CONFIRMAR_EJECUCION=SI; revise el preregistro.")
    if out(["git", "status", "--porcelain"]):
        raise SystemExit("El árbol Git no está limpio: haga commit antes de medir.")
    sha = out(["git", "rev-parse", "HEAD"])
    stamp = datetime.now(timezone.utc).strftime("%Y%m%dT%H%M%SZ")
    directory = EXP / "resultados" / f"{stamp}-{sha[:8]}"
    if directory.exists():
        raise SystemExit("La carpeta de resultados ya existe; no sobrescribir")
    directory.mkdir(parents=True)
    context = {"fecha_utc": stamp, "sha_medido": sha, "sistema": platform.platform(),
               "cpu": platform.processor(), "arquitectura": platform.machine(),
               "docker": out(["docker", "--version"]),
               "compose": out(["docker", "compose", "version"]),
               "niveles_vus": LEVELS, "corridas_por_nivel": RUNS,
               "duracion": DURATION, "k6": "grafana/k6:0.54.0",
               "postgres": "postgres:16-alpine",
               "observaciones_maquina": os.getenv("MAQUINA_NOTAS", ""),
               "generador_y_servidor_en_mismo_host": True}
    (directory / "contexto.json").write_text(json.dumps(context, indent=2, ensure_ascii=False), encoding="utf-8")
    print("Comprobando contenedores y datos existentes; no se vuelve a cargar la semilla.")
    ps = command(["docker", "compose", "ps"], check=False)
    if ps.returncode != 0:
        raise RuntimeError(ps.stderr)
    (directory / "verificacion-semilla.csv").write_text(verify_seed(), encoding="utf-8")
    all_results = []
    for level in LEVELS:
        for number in range(1, RUNS + 1):
            name = f"vu-{level}-corrida-{number}"
            args = ["docker", "compose", "--profile", "load", "run", "--rm",
                    "-e", f"VUS={level}", "-e", f"DURATION={DURATION}",
                    "-v", f"{directory.resolve()}:/evidencia", "k6", "run",
                    f"--summary-export=/evidencia/{name}.json", "carga.js"]
            print(f"Ejecutando {name}")
            # Estado de recursos antes y después; no sustituye un muestreo continuo.
            (directory / f"{name}-docker-antes.txt").write_text(
                out(["docker", "stats", "--no-stream"]), encoding="utf-8")
            result = command(args, check=False)
            (directory / f"{name}.log").write_text(
                result.stdout + "\n" + result.stderr, encoding="utf-8")
            (directory / f"{name}-docker-despues.txt").write_text(
                out(["docker", "stats", "--no-stream"]), encoding="utf-8")
            item = summary(directory / f"{name}.json", level, number, result.returncode)
            all_results.append(item)
            (directory / "corridas.json").write_text(
                json.dumps(all_results, indent=2, ensure_ascii=False), encoding="utf-8")
            if not item["instrumentacion_completa"]:
                raise RuntimeError(f"Instrumentación incompleta en {name}; revisar log. Los archivos quedan guardados.")
    per_level = {}
    for level in LEVELS:
        eligible = [x for x in all_results if x["vus"] == level and
                    x["corrida"] > 1 and x["instrumentacion_completa"]]
        per_level[str(level)] = {"p95_mediano_ms": statistics.median(x["p95_ms"] for x in eligible)
                                 if len(eligible) == 3 else None,
                                 "corridas_validas": len(eligible)}
    (directory / "resumen.json").write_text(
        json.dumps({"niveles": per_level, "nota": "Auditar log, códigos HTTP y recursos antes de interpretar."},
                   indent=2, ensure_ascii=False), encoding="utf-8")
    print(f"Resultados guardados en {directory.relative_to(ROOT)}")


if __name__ == "__main__":
    sys.exit(main())
