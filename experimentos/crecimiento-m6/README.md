# Crecimiento M6 — ejecución pendiente

Protocolo: [00-preregistro.md](00-preregistro.md).

La carga de referencia se ejecuta con `experimentos/consulta-tareas/carga.js`, que admite las variables `VUS`, `DURATION`, `BASE_URL` y `TEST_PASSWORD`. **Aún no existen aquí datos de crecimiento M6.**

Para cada escalón conservar en `resultados/`: resumen original de k6 (`--summary-export`), log de consola, contexto de máquina/versión/semilla, series de recursos si se capturan, tasas de respuesta y verificación de códigos HTTP. No reemplazar `experimentos/consulta-tareas/resultados/`.

Antes de subir datos, revisar que no se registren secretos o tokens. Si el login de preparación falla, una corrida rápida no es evidencia de alto rendimiento.
