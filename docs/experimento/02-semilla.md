# Semilla del experimento

## Registro del ajuste previo a la medición

La hipótesis inicial se conserva sin modificaciones. Antes de ejecutar la primera medición real se revisó la representatividad de la semilla y se reemplazó el escenario de una cuenta con 10.000 tareas por un escenario multiusuario de mayor volumen.

Este cambio se registra antes de obtener resultados para mantener la trazabilidad temporal del experimento.

## Objetivo

Construir un conjunto de datos reproducible para observar el comportamiento de `GET /api/tasks` cuando el sistema contiene miles de cuentas y millones de tareas, mientras cada solicitud recupera únicamente las tareas del usuario autenticado.

## Archivo ejecutable

`experimentos/consulta-tareas/seed.sql`

## Cuentas de prueba

La semilla final contiene 5.000 cuentas experimentales distintas:

`carga0001@aprende.local` hasta `carga5000@aprende.local`.

Cada cuenta tiene correo único. Todas comparten la contraseña de prueba `Aprende123!` porque la contraseña no es una variable del experimento.

El ejecutor crea temporalmente `estudiante@aprende.local` mediante la API para obtener un hash BCrypt válido. La semilla copia ese hash a las cuentas experimentales y elimina después la cuenta temporal, de forma que el conjunto final tenga exactamente 5.000 usuarios experimentales.

## Volumen

- 5.000 usuarios experimentales.
- 5 materias por usuario.
- 25.000 materias en total.
- 1.000 tareas por usuario.
- 5.000.000 de tareas en total.

## Distribución por usuario

Cada usuario tiene cinco materias y 200 tareas en cada una:

| Elemento | Por usuario | Total |
| --- | ---: | ---: |
| Usuarios | 1 | 5.000 |
| Materias | 5 | 25.000 |
| Tareas por materia | 200 | 5.000.000 tareas globales |
| Tareas por usuario | 1.000 | 5.000.000 |

La semilla es sintética. No pretende afirmar que un estudiante real tenga necesariamente 1.000 tareas, sino crear una condición controlada de carga y crecimiento global de datos.

## Reproducibilidad

Antes de insertar la semilla, el script elimina únicamente cuentas de ejecuciones anteriores cuyo correo corresponda al patrón experimental `cargaNNNN@aprende.local`. Las relaciones `ON DELETE CASCADE` limpian sus materias y tareas asociadas.

Después crea nuevamente las 5.000 cuentas, 25.000 materias y 5.000.000 de tareas.

Una nueva ejecución no debe acumular usuarios o tareas de una corrida previa.

## Verificación numérica

La semilla se comprueba con:

`experimentos/consulta-tareas/verificar-semilla.sql`

El ejecutor valida automáticamente que existan:

- 5.000 usuarios;
- 5.000 correos únicos;
- 25.000 materias;
- 5.000.000 de tareas;
- mínimo 1.000 tareas por usuario;
- máximo 1.000 tareas por usuario;
- promedio 1.000 tareas por usuario;
- 5.000 usuarios con exactamente 1.000 tareas.

La evidencia se conserva en:

`experimentos/consulta-tareas/resultados/verificacion-semilla.csv`

## Identidades usadas por k6

Con la configuración predeterminada de 30 VUs, cada VU utiliza una cuenta distinta:

- VU 1 → `carga0001@aprende.local`
- VU 2 → `carga0002@aprende.local`
- ...
- VU 30 → `carga0030@aprende.local`

Esto evita que los 30 clientes compartan una única sesión y permite observar acceso concurrente de identidades independientes.

## Condición de validez

No debe iniciarse la medición si la verificación automática no demuestra exactamente el volumen y distribución definidos arriba.
