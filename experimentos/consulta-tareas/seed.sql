\set ON_ERROR_STOP on

DO $$
DECLARE
    v_user UUID;
    v_subjects UUID[];
BEGIN
    SELECT id INTO v_user FROM app_user WHERE email = 'estudiante@aprende.local';
    IF v_user IS NULL THEN
        RAISE EXCEPTION 'Primero registre estudiante@aprende.local mediante la API';
    END IF;

    DELETE FROM subject WHERE user_id = v_user;

    INSERT INTO subject(user_id, name, instructor)
    SELECT v_user, 'Materia ' || g, 'Docente ' || g
    FROM generate_series(1, 8) AS g;

    SELECT array_agg(id ORDER BY name) INTO v_subjects
    FROM subject WHERE user_id = v_user;

    INSERT INTO task(user_id, subject_id, title, description, due_at, priority, status)
    SELECT v_user, v_subjects[1], 'Tarea M1-' || g, 'Semilla de rendimiento', now() + (g % 30) * interval '1 day',
           CASE WHEN g % 3 = 0 THEN 'ALTA' WHEN g % 3 = 1 THEN 'MEDIA' ELSE 'BAJA' END,
           CASE WHEN g % 4 = 0 THEN 'COMPLETADA' WHEN g % 4 = 1 THEN 'EN_PROGRESO' ELSE 'PENDIENTE' END
    FROM generate_series(1, 8000) AS g;

    INSERT INTO task(user_id, subject_id, title, description, due_at, priority, status)
    SELECT v_user, v_subjects[2], 'Tarea M2-' || g, 'Semilla de rendimiento', now() + (g % 30) * interval '1 day', 'MEDIA', 'PENDIENTE'
    FROM generate_series(1, 750) AS g;

    INSERT INTO task(user_id, subject_id, title, description, due_at, priority, status)
    SELECT v_user, v_subjects[3], 'Tarea M3-' || g, 'Semilla de rendimiento', now() + (g % 30) * interval '1 day', 'MEDIA', 'PENDIENTE'
    FROM generate_series(1, 750) AS g;

    FOR i IN 4..8 LOOP
        INSERT INTO task(user_id, subject_id, title, description, due_at, priority, status)
        SELECT v_user, v_subjects[i], 'Tarea M' || i || '-' || g, 'Semilla de rendimiento',
               now() + (g % 30) * interval '1 day', 'BAJA', 'PENDIENTE'
        FROM generate_series(1, 100) AS g;
    END LOOP;
END $$;
