# Experimento de línea base — consulta de tareas

Este directorio contiene el experimento reproducible del escenario principal de rendimiento del Corte 1.

La hipótesis preregistrada está en `docs/experimento/01-hipotesis-inicial.md`. No debe modificarse después de ejecutar la medición para hacerla coincidir con los resultados.

## Operación medida

```text
GET /api/tasks
```

## Semilla

La semilla utiliza una cuenta de prueba y genera 10.000 tareas distribuidas entre 8 materias:

- Materia 1: 8.000 tareas (80 %).
- Materias 2 y 3: 750 tareas cada una (15 % en total).
- Materias 4 a 8: 100 tareas cada una (5 % en total).

`seed.sql` regenera los datos del usuario `estudiante@aprende.local`. `verificar-semilla.sql` comprueba numéricamente el volumen y la distribución antes de medir.

## Carga

Valores preregistrados por defecto:

```text
30 usuarios virtuales
60 segundos por corrida
4 corridas
```

La corrida 1 es calentamiento. Las corridas 2, 3 y 4 se utilizan para el resultado siempre que no presenten errores. La línea base es la mediana de sus valores p95.

## Requisitos

- Docker Desktop o Docker Engine con `docker compose`.
- Python 3.
- Puerto 8080 disponible para la API.
- Puerto 5432 disponible para PostgreSQL.

No es necesario instalar PostgreSQL ni k6 localmente: se ejecutan en contenedores.

## Ejecutar todo el experimento

Desde la raíz del repositorio:

### Windows PowerShell

```powershell
python .\experimentos\consulta-tareas\ejecutar_experimento.py
```

### Linux/macOS

```bash
python3 ./experimentos/consulta-tareas/ejecutar_experimento.py
```

El ejecutor realiza lo siguiente:

1. construye e inicia PostgreSQL y la API;
2. espera a que `/actuator/health` reporte `UP`;
3. crea o valida el usuario de prueba;
4. carga la semilla;
5. ejecuta la consulta de verificación de distribución;
6. registra las condiciones de la ejecución y el hash Git;
7. ejecuta cuatro corridas k6;
8. conserva la salida cruda de cada corrida;
9. calcula la mediana del p95 de las corridas 2–4.

## Archivos generados

Después de una medición real aparecerán en `resultados/` archivos como:

```text
contexto.json
verificacion-semilla.csv
corrida-1.json
corrida-1.log
corrida-2.json
corrida-2.log
corrida-3.json
corrida-3.log
corrida-4.json
corrida-4.log
resultado.json
```

No edite manualmente los archivos crudos. Deben conservarse para poder rastrear el resultado de línea base.

## Condiciones de validez

Antes de presentar una corrida como válida se debe comprobar que:

- el login responde correctamente;
- `GET /api/tasks` responde HTTP 200;
- las respuestas contienen tareas;
- la semilla tiene 10.000 tareas y la distribución esperada;
- la herramienta no reporta fallos que invaliden la corrida;
- el hash de `contexto.json` corresponde al commit que se está defendiendo.

Una corrida fallida se conserva como evidencia, pero no se presenta como una corrida exitosa.
