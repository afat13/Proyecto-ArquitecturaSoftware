ALTER TABLE subject_participant
    ADD COLUMN role VARCHAR(120) NOT NULL DEFAULT '';

CREATE UNIQUE INDEX uq_task_user_utadeo_assignment
    ON task(user_id, utadeo_assignment_id)
    WHERE utadeo_assignment_id IS NOT NULL;
