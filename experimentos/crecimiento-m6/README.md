# Prueba de crecimiento del Módulo 6

Usamos el mismo backend y la misma semilla del experimento M2, pero esta vez subimos la concurrencia por escalones. El diseño está en [00-preregistro.md](00-preregistro.md) y quedó fijado en el commit [007defa](https://github.com/afat13/Proyecto-ArquitecturaSoftware/commit/007defa5e6b1de8672516d6607125335b927b8e0), **anterior al script de ejecución**.

## Preparación

Necesitas Docker Desktop, Python 3, Git y el proyecto actualizado desde `main`. Ejecuta desde la raíz:

```powershell
git pull origin main
docker compose up -d --build db api
docker compose ps
```

El ejecutor **no vuelve a sembrar** las cinco millones de tareas, ni modifica los resultados de M2. Primero comprueba que los datos ya existan. Si los datos no están cargados, se debe preparar una base experimental aislada usando el procedimiento documentado en [el experimento original](../consulta-tareas/README.md). No ejecutar sobre una base con datos reales.

## Ejecución

```powershell
$env:M6_CONFIRMAR_EJECUCION="SI"
$env:MAQUINA_NOTAS="Indicar CPU, RAM, limite de Docker y otras cargas del equipo"
python .\experimentos\crecimiento-m6\ejecutar_crecimiento.py
```

El archivo `ejecutar_crecimiento.py` inicia 4 corridas por cada nivel de 30, 50, 100, 150 y 200 VUs, con 60 segundos por corrida. Se genera una carpeta nueva `resultados/<fecha>-<sha>/` para no sobrescribir pruebas. La primera corrida de cada nivel es calentamiento.

Se guardan la verificación de semilla, parámetros, SHA, archivos JSON y logs de k6, conteos HTTP que reporte el instrumento, resultados por corrida y capturas puntuales de `docker stats` antes y después. Esas capturas **no equivalen a una serie continua de utilización**; si hace falta justificar un cuello de botella se debe instrumentar esa parte adicionalmente.

Si falla la verificación, el login o una corrida, conservar la carpeta generada y explicar lo ocurrido. No cambiar la hipótesis ni borrar datos fallidos. Los checks fallidos durante la carga cuentan como degradación observable si el generador y los datos eran válidos.

## Después de ejecutar

1. Revisar `corridas.json`, `resumen.json` y cada `.log`, incluidos códigos HTTP, checks y errores.
2. Verificar si Docker o el mismo computador se estaban saturando. No atribuir el problema a Spring Boot o PostgreSQL sin evidencia.
3. Guardar los resultados sin contraseñas ni tokens y subirlos a `/experimentos/crecimiento-m6/resultados/`.
4. Elaborar las matrices y completar el ADR con esos datos. Hasta hacerlo, M6 sigue en ejecución pendiente.
