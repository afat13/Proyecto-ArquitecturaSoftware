SELECT
    s.name AS materia,
    count(t.id) AS tareas,
    round(count(t.id) * 100.0 / sum(count(t.id)) OVER (), 2) AS porcentaje
FROM subject s
LEFT JOIN task t ON t.subject_id = s.id
JOIN app_user u ON u.id = s.user_id
WHERE u.email = 'estudiante@aprende.local'
GROUP BY s.id, s.name
ORDER BY tareas DESC, materia;

SELECT count(*) AS total_tareas
FROM task t
JOIN app_user u ON u.id = t.user_id
WHERE u.email = 'estudiante@aprende.local';
