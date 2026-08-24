\set ON_ERROR_STOP on

BEGIN;

CREATE TEMP TABLE seed_password AS
SELECT password_hash
FROM app_user
WHERE email = 'estudiante@aprende.local';

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM seed_password) THEN
        RAISE EXCEPTION 'Primero registre estudiante@aprende.local mediante la API';
    END IF;
END $$;

-- Elimina únicamente datos de ejecuciones anteriores de esta semilla.
DELETE FROM app_user
WHERE email ~ '^carga[0-9]{4}@aprende[.]local$';

-- 5.000 cuentas distintas. Todas reutilizan el hash de la contraseña de prueba
-- del usuario bootstrap; el correo sí es único para cada identidad experimental.
INSERT INTO app_user(email, password_hash, first_name, last_name, phone)
SELECT
    'carga' || to_char(g, 'FM0000') || '@aprende.local',
    (SELECT password_hash FROM seed_password LIMIT 1),
    'Usuario',
    'Carga ' || g,
    NULL
FROM generate_series(1, 5000) AS g;

-- Cinco materias por usuario: 25.000 materias en total.
INSERT INTO subject(user_id, name, instructor)
SELECT
    u.id,
    'Materia ' || m,
    'Docente ' || m
FROM app_user u
CROSS JOIN generate_series(1, 5) AS m
WHERE u.email ~ '^carga[0-9]{4}@aprende[.]local$';

-- 200 tareas por materia x 5 materias = 1.000 tareas por usuario.
-- Total esperado: 5.000.000 de tareas.
INSERT INTO task(user_id, subject_id, title, description, due_at, priority, status)
SELECT
    s.user_id,
    s.id,
    'Tarea ' || s.name || '-' || g,
    'Semilla multiusuario de rendimiento',
    now() + (g % 30) * interval '1 day',
    CASE WHEN g % 3 = 0 THEN 'ALTA' WHEN g % 3 = 1 THEN 'MEDIA' ELSE 'BAJA' END,
    CASE WHEN g % 4 = 0 THEN 'COMPLETADA' WHEN g % 4 = 1 THEN 'EN_PROGRESO' ELSE 'PENDIENTE' END
FROM subject s
JOIN app_user u ON u.id = s.user_id
CROSS JOIN generate_series(1, 200) AS g
WHERE u.email ~ '^carga[0-9]{4}@aprende[.]local$';

-- El usuario bootstrap solo sirve para obtener un BCrypt válido. Se elimina para
-- que la semilla final tenga exactamente 5.000 usuarios experimentales.
DELETE FROM app_user WHERE email = 'estudiante@aprende.local';

COMMIT;
