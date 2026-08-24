WITH usuarios_experimento AS (
    SELECT id, email
    FROM app_user
    WHERE email ~ '^carga[0-9]{4}@aprende[.]local$'
), tareas_por_usuario AS (
    SELECT u.id, count(t.id) AS tareas
    FROM usuarios_experimento u
    LEFT JOIN task t ON t.user_id = u.id
    GROUP BY u.id
)
SELECT
    (SELECT count(*) FROM usuarios_experimento) AS usuarios,
    (SELECT count(DISTINCT email) FROM usuarios_experimento) AS correos_unicos,
    (SELECT count(*) FROM subject s JOIN usuarios_experimento u ON u.id = s.user_id) AS materias,
    (SELECT count(*) FROM task t JOIN usuarios_experimento u ON u.id = t.user_id) AS total_tareas,
    (SELECT min(tareas) FROM tareas_por_usuario) AS min_tareas_usuario,
    (SELECT max(tareas) FROM tareas_por_usuario) AS max_tareas_usuario,
    (SELECT round(avg(tareas), 2) FROM tareas_por_usuario) AS promedio_tareas_usuario,
    (SELECT count(*) FROM tareas_por_usuario WHERE tareas = 1000) AS usuarios_con_1000_tareas;
