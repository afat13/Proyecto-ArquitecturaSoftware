# Comparación antes/después: paginación del módulo Tareas

## Qué se cambió

Se añadió `GET /api/tasks/page?page=0&size=50`. Devuelve `{items, page, size, mayHaveMore}` y no altera el antiguo `GET /api/tasks`, que Android todavía usa para clasificar todas las tareas de hoy y vencidas. Hay límite de 100 registros por página y un orden determinista (`due_at`, `created_at`, `id`). El backend sigue usando Spring Boot y PostgreSQL; no se han introducido microservicios ni caché.

**Limitación conocida:** `mayHaveMore` indica que llegaron `size` registros, no garantiza que exista una página adicional. `OFFSET` puede duplicar u omitir elementos si se insertan o eliminan tareas entre páginas; una adopción definitiva necesita estrategia de actualización/cursor, filtros por estado y cambios en Android.

## Qué vamos a medir

La mejora esperada es una reducción del tamaño de respuesta y del tiempo por **consulta de una página**. No equivale a comparar la recuperación de 1.000 tareas frente a la recuperación de las mismas 1.000 tareas mediante 20 páginas. Son cargas funcionales diferentes. Ambas deben reportarse con claridad.

Comparar bajo la misma máquina, datos, commit de backend y nivel de concurrencia:

- Control: `GET /api/tasks` devuelve 1.000 tareas.
- Variante: `GET /api/tasks/page?page=0&size=50` devuelve 50.
- Misma carga y duración; medir p50/p95 de GET (no de login), GET/s, errores, HTTP y bytes recibidos.
- Cuatro corridas por caso: la primera calentamiento, las otras tres medidas. Conservar resultados crudos y el SHA exacto. Repetir por los niveles que puedan soportarse sin saturar el generador.

## Cómo lanzarlo desde PowerShell

Primero actualiza el código e inicia API y base de datos con la semilla de prueba ya instalada:

```powershell
git pull origin main
docker compose up -d --build db api
```

Ejemplo para 30 VUs y 60 segundos, guardando archivos en una carpeta nueva:

```powershell
New-Item -ItemType Directory -Force experimentos/crecimiento-m6/comparacion-paginacion | Out-Null
```

**Montaje del instrumento:** la imagen k6 del Compose monta por defecto `./experimentos/consulta-tareas:/work`. Para usar este script en `experimentos/crecimiento-m6`, ejecutar el comando documentado a continuación, que añade un montaje explícito de lectura:

```powershell
docker compose --profile load run --rm -e VUS=30 -e DURATION=60s -e TASKS_MODE=legacy -v "${PWD}/experimentos/crecimiento-m6:/m6:ro" -v "${PWD}/experimentos/crecimiento-m6/comparacion-paginacion:/evidencia" k6 run --summary-export=/evidencia/legacy-30-1.json /m6/comparar-paginacion.js
docker compose --profile load run --rm -e VUS=30 -e DURATION=60s -e TASKS_MODE=paged -v "${PWD}/experimentos/crecimiento-m6:/m6:ro" -v "${PWD}/experimentos/crecimiento-m6/comparacion-paginacion:/evidencia" k6 run --summary-export=/evidencia/paged-30-1.json /m6/comparar-paginacion.js
```

Hacer cuatro corridas por variante, conservando logs completos. No subir tokens que puedan quedar en `setup_data` de k6: preparar una copia depurada, mantener los originales de manera segura y revisar también el historial remoto.

## Criterios para interpretar

Comprobar todos los códigos HTTP y checks. Registrar recursos continuos de API, DB y generador cuando sea posible. La diferencia de p95 de una página de 50 frente a 1.000 tareas no demuestra por sí sola un ahorro total para quien navega todas las páginas.

**Estado actual:** el endpoint y el instrumento están en código; no se han ejecutado ni validado aún mediciones comparativas.
