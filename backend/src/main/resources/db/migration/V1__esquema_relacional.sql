CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE app_user (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone VARCHAR(40),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE auth_session (
    token_hash CHAR(64) PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_auth_session_user ON auth_session(user_id);
CREATE INDEX idx_auth_session_expiry ON auth_session(expires_at);

CREATE TABLE subject (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    name VARCHAR(180) NOT NULL,
    instructor VARCHAR(180),
    utadeo_id INTEGER,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE(user_id, utadeo_id)
);

CREATE INDEX idx_subject_user ON subject(user_id);

CREATE TABLE subject_topic (
    subject_id UUID NOT NULL REFERENCES subject(id) ON DELETE CASCADE,
    position INTEGER NOT NULL CHECK (position >= 0),
    topic VARCHAR(255) NOT NULL,
    PRIMARY KEY(subject_id, position)
);

CREATE TABLE subject_participant (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    subject_id UUID NOT NULL REFERENCES subject(id) ON DELETE CASCADE,
    external_id VARCHAR(120),
    name VARCHAR(180) NOT NULL,
    email VARCHAR(255)
);

CREATE INDEX idx_subject_participant_subject ON subject_participant(subject_id);

CREATE TABLE task (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    subject_id UUID NOT NULL REFERENCES subject(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL DEFAULT '',
    due_at TIMESTAMPTZ,
    priority VARCHAR(20) NOT NULL DEFAULT 'MEDIA' CHECK (priority IN ('ALTA','MEDIA','BAJA')),
    status VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE' CHECK (status IN ('PENDIENTE','EN_PROGRESO','COMPLETADA')),
    utadeo_assignment_id VARCHAR(120),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_task_user ON task(user_id);
CREATE INDEX idx_task_user_due ON task(user_id, due_at);
CREATE INDEX idx_task_subject ON task(subject_id);
CREATE INDEX idx_task_status ON task(user_id, status);

CREATE TABLE daily_challenge (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    challenge_date DATE NOT NULL,
    total_subjects INTEGER NOT NULL DEFAULT 0 CHECK (total_subjects >= 0),
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE(user_id, challenge_date)
);

CREATE TABLE challenge_subject_completion (
    challenge_id UUID NOT NULL REFERENCES daily_challenge(id) ON DELETE CASCADE,
    subject_id UUID NOT NULL REFERENCES subject(id) ON DELETE CASCADE,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    completed_at TIMESTAMPTZ,
    PRIMARY KEY(challenge_id, subject_id)
);

CREATE TABLE challenge_question (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    challenge_id UUID NOT NULL REFERENCES daily_challenge(id) ON DELETE CASCADE,
    subject_id UUID NOT NULL REFERENCES subject(id) ON DELETE CASCADE,
    question TEXT NOT NULL,
    option_a TEXT NOT NULL,
    option_b TEXT NOT NULL,
    option_c TEXT NOT NULL,
    option_d TEXT NOT NULL,
    correct_option SMALLINT NOT NULL CHECK (correct_option BETWEEN 0 AND 3),
    explanation TEXT NOT NULL DEFAULT '',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_challenge_user_date ON daily_challenge(user_id, challenge_date);
CREATE INDEX idx_question_challenge_subject ON challenge_question(challenge_id, subject_id);
