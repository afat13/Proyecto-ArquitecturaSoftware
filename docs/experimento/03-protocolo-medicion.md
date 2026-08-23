# Protocolo de medición de línea base

## Objetivo

Obtener una línea base reproducible del tiempo de respuesta de `GET /api/tasks` bajo carga concurrente y con la semilla registrada para el Corte 1.

## Referencias

- Hipótesis: `docs/experimento/01-hipotesis-inicial.md`
- Semilla: `experimentos/consulta-tareas/seed.sql`
- Verificación: `experimentos/consulta-tareas/verificar-semilla.sql`
- Instrumento: `experimentos/consulta-tareas/carga.js`
- Ejecutor: `experimentos/consulta-tareas/ejecutar_experimento.py`
- Resumen: `experimentos/consulta-tareas/resumir_resultados.py`

## Herramienta de carga

k6, ejecutado mediante Docker para no depender de una instalación global.

## Parámetros preregistrados

- 30 usuarios virtuales.
- 60 segundos por corrida.
- 4 corridas en total.
- Corrida 1: calentamiento y descartada del resultado final.
- Corridas 2, 3 y 4: candidatas a válidas.

## Métrica principal

p95 de `http_req_duration` para la operación de consulta de tareas, expresado en milisegundos.

## Métricas secundarias

- mediana;
- promedio;
- solicitudes por segundo;
- tasa de fallos HTTP;
- checks exitosos y fallidos.

## Validaciones del instrumento

Durante el `setup`, k6 debe comprobar que `POST /api/auth/login` responda 200 y entregue un token.

Durante la carga, cada solicitud debe comprobar que:

1. `GET /api/tasks` responda 200;
2. el cuerpo pueda interpretarse como arreglo;
3. la respuesta contenga tareas.

## Procedimiento

1. Ubicarse en la raíz del commit que se desea medir.
2. Comprobar que Docker esté disponible.
3. No modificar la hipótesis preregistrada.
4. Ejecutar en Windows:

```powershell
python .\experimentos\consulta-tareas\ejecutar_experimento.py
```

En Linux/macOS:

```bash
python3 ./experimentos/consulta-tareas/ejecutar_experimento.py
```

5. El ejecutor inicia y construye PostgreSQL y la API.
6. Espera a que `/actuator/health` reporte `UP`.
7. Crea o valida el usuario de experimento.
8. Regenera la semilla.
9. Ejecuta la verificación SQL de volumen y distribución.
10. Guarda contexto de máquina, fecha y hash Git.
11. Ejecuta cuatro corridas.
12. Conserva JSON y logs crudos de cada corrida.
13. Calcula un resumen sin modificar los archivos originales.

## Resultado de línea base

Si las corridas 2, 3 y 4 son válidas:

`línea base = mediana(p95 corrida 2, p95 corrida 3, p95 corrida 4)`

## Corrida inválida

Una corrida no se presenta como exitosa si falla el login, existen respuestas HTTP inválidas, la semilla es incorrecta, API o DB se caen, k6 falla o cambian las condiciones sin registrarlo. Los archivos originales deben conservarse incluso si una corrida resulta inválida.

## Principio de interpretación

La medición describe comportamiento observado bajo condiciones registradas. No identifica por sí sola una causa.
