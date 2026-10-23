# Fitness function del Módulo 6

El resultado de crecimiento todavía no permite saber cuál componente limita primero. La propiedad que convenga proteger debe venir de la decisión final del [ADR-007](../adr/ADR-007-distribucion-resiliencia-modulo6.md), no de un test añadido solo para cumplir.

## Definición candidata para discutir con el equipo

**Decisión candidata:** mantener Tareas y Materias como responsabilidades lógicas dentro del mismo backend hasta que la evidencia justifique extraer un servicio.

**Propiedad a proteger:** las dependencias de la capa de API deben seguir pasando por los componentes del backend y no incorporar conexiones directas desde Android a PostgreSQL.

**Regla automatizable propuesta:** verificar que el módulo Android no declare el driver JDBC de PostgreSQL ni una URL de conexión JDBC en su código. Es una regla sobre una frontera existente y puede comprobarse automáticamente, pero **no demuestra** la modularidad entre Tareas y Materias.

**Vínculo al ADR:** [ADR-007](../adr/ADR-007-distribucion-resiliencia-modulo6.md); formalizar esta elección cuando el equipo apruebe una decisión.

**Prueba negativa:** introducir temporalmente una dependencia JDBC en el build Android en un entorno controlado y comprobar que CI la rechace; revertir el cambio. No crear evidencia de fallo ficticia ni dejar la violación en `main`.

## Estado

Fitness **candidata definida para revisión**, no implementada. El sílabo admite su implementación definitiva hasta semana 14. Registrar SHA de test verde, ejecución roja controlada y recuperación verde cuando se implemente.
