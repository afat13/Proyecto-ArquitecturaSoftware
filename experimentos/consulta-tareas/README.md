# Experimento de línea base — consulta de tareas

Este directorio contiene el experimento reproducible del escenario principal de rendimiento del Corte 1.

La hipótesis inicial está en `docs/experimento/01-hipotesis-inicial.md` y se conserva sin modificaciones. Antes de la primera medición se revisó la semilla y el ajuste quedó documentado en `docs/experimento/02-semilla.md`.

## Operación medida

```text
GET /api/tasks
```

## Semilla

La semilla genera:

- 5.000 cuentas experimentales distintas;
- 5 materias por cuenta;
- 25.000 materias en total;
- 1.000 tareas por usuario;
- 5.000.000 de tareas en total.

Las cuentas siguen el patrón `carga0001@aprende.local` hasta `carga5000@aprende.local`.

`seed.sql` regenera los datos experimentales. `verificar-semilla.sql` comprueba numéricamente el volumen antes de medir y el ejecutor aborta si no coincide con lo esperado.

## Carga

Valores por defecto:

```text
30 usuarios virtuales
60 segundos por corrida
4 corridas
```

Cada VU inicia sesión con una cuenta distinta, por lo que los 30 usuarios virtuales no comparten una única sesión.

La corrida 1 es calentamiento. Las corridas 2, 3 y 4 se utilizan para el resultado siempre que sean válidas. La línea base es la mediana de sus valores p95.

## Requisitos

- Docker Desktop o Docker Engine con `docker compose`.
- Python 3.
- Puerto 8080 disponible para la API.
- Puerto 5432 disponible para PostgreSQL.
- Espacio en disco suficiente para PostgreSQL con cinco millones de filas de tareas.

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

El ejecutor:

1. construye e inicia PostgreSQL y la API;
2. espera a que `/actuator/health` reporte `UP`;
3. crea un usuario bootstrap temporal para disponer de un hash BCrypt válido;
4. genera las 5.000 cuentas experimentales;
5. genera 25.000 materias y 5.000.000 de tareas;
6. verifica automáticamente el volumen y las 1.000 tareas por usuario;
7. registra máquina, fecha, parámetros y hash Git;
8. ejecuta cuatro corridas k6 con identidades independientes;
9. conserva la salida cruda;
10. calcula la mediana del p95 de las corridas 2–4.

La primera carga de cinco millones de tareas puede tardar varios minutos según CPU, disco y memoria disponibles.

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

No edite manualmente los archivos crudos.

## Condiciones de validez

Antes de presentar una corrida como válida se debe comprobar que:

- los logins de las identidades usadas responden correctamente;
- `GET /api/tasks` responde HTTP 200;
- cada respuesta contiene exactamente 1.000 tareas;
- existen 5.000 usuarios experimentales con correos únicos;
- existen 25.000 materias;
- existen 5.000.000 de tareas;
- cada usuario tiene exactamente 1.000 tareas;
- k6 no reporta fallos que invaliden la corrida;
- el hash de `contexto.json` corresponde al commit medido.

Una corrida fallida se conserva como evidencia, pero no se presenta como una corrida exitosa.
