# Protocolo de medición de línea base

## Objetivo

Obtener una línea base reproducible del tiempo de respuesta de `GET /api/tasks` bajo carga concurrente sobre una base con 5.000 usuarios y 5.000.000 de tareas.

## Referencias

- Hipótesis: `docs/experimento/01-hipotesis-inicial.md`
- Semilla documentada: `docs/experimento/02-semilla.md`
- SQL de semilla: `experimentos/consulta-tareas/seed.sql`
- Verificación: `experimentos/consulta-tareas/verificar-semilla.sql`
- Carga: `experimentos/consulta-tareas/carga.js`
- Ejecutor: `experimentos/consulta-tareas/ejecutar_experimento.py`
- Resumen: `experimentos/consulta-tareas/resumir_resultados.py`

## Condición de datos

- 5.000 cuentas distintas.
- 5 materias por cuenta.
- 1.000 tareas por cuenta.
- 25.000 materias.
- 5.000.000 de tareas.

La modificación de la semilla se realizó antes de la primera medición y la hipótesis inicial permanece intacta.

## Parámetros de carga

- 30 usuarios virtuales por defecto.
- Una cuenta distinta por VU.
- 60 segundos por corrida.
- 4 corridas.
- Corrida 1: calentamiento, excluida del resultado.
- Corridas 2, 3 y 4: usadas para la línea base si son válidas.

## Métrica principal

p95 de `http_req_duration` para `GET /api/tasks`, en milisegundos.

## Métricas secundarias

- mediana;
- promedio;
- solicitudes por segundo;
- tasa de fallos HTTP;
- checks exitosos y fallidos.

## Validaciones

En `setup`, k6 inicia sesión con una cuenta diferente para cada VU y comprueba respuesta 200 y presencia de token.

Durante la carga se valida que cada `GET /api/tasks`:

1. responda 200;
2. devuelva un arreglo JSON;
3. contenga exactamente 1.000 tareas.

Antes de iniciar k6, el ejecutor valida automáticamente que la semilla tenga exactamente 5.000 usuarios únicos, 25.000 materias, 5.000.000 de tareas y 1.000 tareas para cada usuario.

## Ejecución

Windows:

```powershell
python .\experimentos\consulta-tareas\ejecutar_experimento.py
```

Linux/macOS:

```bash
python3 ./experimentos/consulta-tareas/ejecutar_experimento.py
```

El ejecutor construye API y PostgreSQL, carga y verifica la semilla, registra fecha, máquina, commit y parámetros, realiza las cuatro corridas y conserva los JSON y logs originales.

La creación inicial de cinco millones de tareas puede tardar varios minutos y requiere espacio suficiente en disco.

## Resultado

Si las corridas 2, 3 y 4 son válidas:

`línea base = mediana(p95 corrida 2, p95 corrida 3, p95 corrida 4)`

## Corrida inválida

Una corrida no se acepta si falla algún login, hay respuestas HTTP inválidas, una respuesta no contiene 1.000 tareas, la semilla no coincide con el volumen esperado, la API o PostgreSQL fallan, k6 termina con error o cambian condiciones sin registrarlas.

## Interpretación

El resultado describe `GET /api/tasks` para usuarios con 1.000 tareas dentro de una base con cinco millones de tareas. No identifica por sí solo una causa de degradación ni representa todos los posibles tamaños de producción.
