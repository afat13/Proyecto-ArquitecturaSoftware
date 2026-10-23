# Fitness function de M6

**Estado:** criterio de selección preparado, implementación condicionada a la decisión del ADR-007.

## Cadena obligatoria
1. Elegir en [ADR-007](../adr/ADR-007-distribucion-resiliencia-modulo6.md) una propiedad estructural verificable que resulte crítica.
2. Expresar regla inequívoca en términos de dependencias reales del código (paquetes, módulos, controladores, persistencia).
3. Implementarla como prueba automatizada en el backend y ejecutarla desde el workflow `.github/workflows/ci.yml`.
4. Verificar estado verde sobre la versión válida.
5. Introducir **en rama temporal separada** una infracción deliberada y conservar el enlace al job rojo, con SHA de la infracción. Revertir la infracción; comprobar verde otra vez.

## Hoja de registro

| Elemento | Evidencia |
|---|---|
| Decisión protegida | PENDIENTE |
| Propiedad / regla formal | PENDIENTE |
| Nombre de la prueba | PENDIENTE |
| Commit verde y workflow | PENDIENTE |
| Commit con violación y workflow rojo | PENDIENTE |
| Reversión y workflow verde | PENDIENTE |

No registrar una prueba de estructura simplemente porque sea fácil de programar. No confundir que el workflow exista con demostrar el control negativo.
