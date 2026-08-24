#!/usr/bin/env python3
"""Ejecuta de forma reproducible la línea base de GET /api/tasks.

Requiere Python 3 y Docker Desktop/Engine con `docker compose` disponible.
No modifica la hipótesis preregistrada y conserva cada salida cruda de k6.
"""

from __future__ import annotations

import csv
import ctypes
import io
import json
import os
import platform
import subprocess
import sys
import time
import urllib.error
import urllib.request
from datetime import datetime, timezone
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
EXP = Path(__file__).resolve().parent
RESULTS = EXP / "resultados"
BASE_URL = os.environ.get("BASE_URL_HOST", "http://localhost:8080")
BOOTSTRAP_EMAIL = "estudiante@aprende.local"
PASSWORD = "Aprende123!"
DB_USER = os.environ.get("POSTGRES_USER", "aprende")
DB_NAME = os.environ.get("POSTGRES_DB", "aprende_aprender")
RUNS = 4
EXPECTED_USERS = 5000
EXPECTED_SUBJECTS = 25000
EXPECTED_TASKS_PER_USER = 1000
EXPECTED_TASKS = EXPECTED_USERS * EXPECTED_TASKS_PER_USER


def run(command: list[str], *, input_text: str | None = None, capture: bool = True) -> subprocess.CompletedProcess[str]:
    print("+", " ".join(command))
    return subprocess.run(
        command,
        cwd=ROOT,
        input=input_text,
        text=True,
        check=True,
        capture_output=capture,
    )


def output(command: list[str]) -> str:
    try:
        return run(command).stdout.strip()
    except Exception as exc:
        return f"no disponible: {exc}"


def total_memory_bytes() -> int | None:
    try:
        if os.name == "nt":
            class MemoryStatus(ctypes.Structure):
                _fields_ = [
                    ("dwLength", ctypes.c_ulong),
                    ("dwMemoryLoad", ctypes.c_ulong),
                    ("ullTotalPhys", ctypes.c_ulonglong),
                    ("ullAvailPhys", ctypes.c_ulonglong),
                    ("ullTotalPageFile", ctypes.c_ulonglong),
                    ("ullAvailPageFile", ctypes.c_ulonglong),
                    ("ullTotalVirtual", ctypes.c_ulonglong),
                    ("ullAvailVirtual", ctypes.c_ulonglong),
                    ("ullAvailExtendedVirtual", ctypes.c_ulonglong),
                ]
            status = MemoryStatus()
            status.dwLength = ctypes.sizeof(MemoryStatus)
            ctypes.windll.kernel32.GlobalMemoryStatusEx(ctypes.byref(status))
            return int(status.ullTotalPhys)
        page_size = os.sysconf("SC_PAGE_SIZE")
        pages = os.sysconf("SC_PHYS_PAGES")
        return int(page_size * pages)
    except Exception:
        return None


def request_json(path: str, payload: dict) -> tuple[int, dict | str]:
    data = json.dumps(payload).encode("utf-8")
    req = urllib.request.Request(
        BASE_URL + path,
        data=data,
        headers={"Content-Type": "application/json"},
        method="POST",
    )
    try:
        with urllib.request.urlopen(req, timeout=15) as response:
            raw = response.read().decode("utf-8")
            return response.status, json.loads(raw) if raw else {}
    except urllib.error.HTTPError as exc:
        raw = exc.read().decode("utf-8")
        try:
            body = json.loads(raw)
        except Exception:
            body = raw
        return exc.code, body


def wait_for_api(timeout_seconds: int = 180) -> None:
    deadline = time.time() + timeout_seconds
    while time.time() < deadline:
        try:
            with urllib.request.urlopen(BASE_URL + "/actuator/health", timeout=3) as response:
                if response.status == 200 and "UP" in response.read().decode("utf-8"):
                    return
        except Exception:
            pass
        time.sleep(3)
    raise RuntimeError("La API no alcanzó estado UP dentro del tiempo esperado")


def ensure_bootstrap_user() -> None:
    status, _ = request_json(
        "/api/auth/register",
        {
            "email": BOOTSTRAP_EMAIL,
            "password": PASSWORD,
            "firstName": "Estudiante",
            "lastName": "Prueba",
            "phone": "",
        },
    )
    if status in (200, 201):
        print("Usuario bootstrap del experimento creado")
        return

    login_status, login_body = request_json(
        "/api/auth/login",
        {"email": BOOTSTRAP_EMAIL, "password": PASSWORD},
    )
    if login_status != 200:
        raise RuntimeError(
            f"No se pudo crear ni autenticar el usuario bootstrap. "
            f"registro={status}, login={login_status}, respuesta={login_body}"
        )
    print("Usuario bootstrap ya existente y autenticable")


def load_seed() -> None:
    sql = (EXP / "seed.sql").read_text(encoding="utf-8")
    print("Cargando 5.000 usuarios y 5.000.000 de tareas; esta etapa puede tardar varios minutos.")
    run(
        ["docker", "compose", "exec", "-T", "db", "psql", "-v", "ON_ERROR_STOP=1", "-U", DB_USER, "-d", DB_NAME],
        input_text=sql,
    )


def verify_seed() -> str:
    sql = (EXP / "verificar-semilla.sql").read_text(encoding="utf-8")
    result = run(
        [
            "docker", "compose", "exec", "-T", "db", "psql",
            "-v", "ON_ERROR_STOP=1", "-U", DB_USER, "-d", DB_NAME,
            "--csv",
        ],
        input_text=sql,
    )

    rows = list(csv.DictReader(io.StringIO(result.stdout)))
    if len(rows) != 1:
        raise RuntimeError("La verificación de la semilla no devolvió exactamente una fila")

    row = rows[0]
    expected = {
        "usuarios": EXPECTED_USERS,
        "correos_unicos": EXPECTED_USERS,
        "materias": EXPECTED_SUBJECTS,
        "total_tareas": EXPECTED_TASKS,
        "min_tareas_usuario": EXPECTED_TASKS_PER_USER,
        "max_tareas_usuario": EXPECTED_TASKS_PER_USER,
        "usuarios_con_1000_tareas": EXPECTED_USERS,
    }

    for field, expected_value in expected.items():
        actual = int(float(row[field]))
        if actual != expected_value:
            raise RuntimeError(
                f"Semilla inválida: {field}={actual}, esperado={expected_value}"
            )

    if float(row["promedio_tareas_usuario"]) != float(EXPECTED_TASKS_PER_USER):
        raise RuntimeError(
            f"Semilla inválida: promedio_tareas_usuario={row['promedio_tareas_usuario']}, "
            f"esperado={EXPECTED_TASKS_PER_USER}"
        )

    print("Semilla verificada: 5.000 usuarios únicos y 5.000.000 de tareas")
    return result.stdout


def capture_context() -> dict:
    return {
        "fecha_utc": datetime.now(timezone.utc).isoformat(),
        "commit_medido": output(["git", "rev-parse", "HEAD"]),
        "rama": output(["git", "branch", "--show-current"]),
        "sistema_operativo": platform.platform(),
        "arquitectura": platform.machine(),
        "procesador": platform.processor() or "no informado por el sistema",
        "ram_bytes": total_memory_bytes(),
        "python": platform.python_version(),
        "docker": output(["docker", "--version"]),
        "docker_compose": output(["docker", "compose", "version"]),
        "postgresql": "postgres:16-alpine",
        "operacion": "GET /api/tasks",
        "usuarios_virtuales": int(os.environ.get("K6_VUS", "30")),
        "identidades_k6": "una cuenta distinta por VU",
        "duracion": os.environ.get("K6_DURATION", "60s"),
        "corridas": RUNS,
        "corrida_calentamiento": 1,
        "usuarios_semilla": EXPECTED_USERS,
        "materias_por_usuario": 5,
        "tareas_por_usuario": EXPECTED_TASKS_PER_USER,
        "volumen_tareas_total": EXPECTED_TASKS,
        "notas_maquina": os.environ.get("MAQUINA_NOTAS", ""),
    }


def execute_k6(run_number: int) -> None:
    json_name = f"corrida-{run_number}.json"
    log_path = RESULTS / f"corrida-{run_number}.log"
    command = [
        "docker", "compose", "--profile", "load", "run", "--rm", "k6",
        "run", f"--summary-export=/work/resultados/{json_name}", "carga.js",
    ]
    print("+", " ".join(command))
    completed = subprocess.run(command, cwd=ROOT, text=True, capture_output=True)
    log_path.write_text(completed.stdout + "\n" + completed.stderr, encoding="utf-8")
    if completed.returncode != 0:
        raise RuntimeError(
            f"La corrida {run_number} terminó con error. Revise {log_path.relative_to(ROOT)}"
        )


def main() -> int:
    RESULTS.mkdir(parents=True, exist_ok=True)

    vus = int(os.environ.get("K6_VUS", "30"))
    if vus < 1 or vus > EXPECTED_USERS:
        raise RuntimeError(f"K6_VUS debe estar entre 1 y {EXPECTED_USERS}")

    run(["docker", "compose", "up", "-d", "--build", "db", "api"], capture=False)
    wait_for_api()
    ensure_bootstrap_user()
    load_seed()

    verification = verify_seed()
    (RESULTS / "verificacion-semilla.csv").write_text(verification, encoding="utf-8")
    (RESULTS / "contexto.json").write_text(
        json.dumps(capture_context(), ensure_ascii=False, indent=2),
        encoding="utf-8",
    )

    for run_number in range(1, RUNS + 1):
        print(f"\n=== Corrida {run_number}/{RUNS} ===")
        execute_k6(run_number)
        if run_number < RUNS:
            time.sleep(10)

    run([sys.executable, str(EXP / "resumir_resultados.py")], capture=False)
    print("\nExperimento finalizado. Conserve todos los archivos de resultados sin editarlos.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
