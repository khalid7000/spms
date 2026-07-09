-- End-of-year evaluation workflow: employee self-assesses categories against criteria-tagged
-- achievements, head rates every criterion/category plus an overall rank, then either party signs
-- (or the employee refuses to sign with a rationale). One row per employee per academic year.

CREATE TABLE annual_evaluation (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES app_user(id),
    head_id BIGINT NOT NULL REFERENCES app_user(id),
    academic_year_id BIGINT NOT NULL REFERENCES academic_year(id),
    state VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    head_overall_rank INT,
    employee_submitted_at TIMESTAMP,
    head_submitted_at TIMESTAMP,
    head_signed_at TIMESTAMP,
    employee_signed_at TIMESTAMP,
    employee_refused BOOLEAN NOT NULL DEFAULT FALSE,
    employee_refusal_rationale TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (employee_id, academic_year_id)
);

CREATE TABLE annual_evaluation_category_result (
    id BIGSERIAL PRIMARY KEY,
    evaluation_id BIGINT NOT NULL REFERENCES annual_evaluation(id) ON DELETE CASCADE,
    category_id BIGINT NOT NULL REFERENCES portfolio_category(id),
    employee_self_rank INT,
    head_category_rank INT,
    UNIQUE (evaluation_id, category_id)
);

CREATE TABLE annual_evaluation_criteria_result (
    id BIGSERIAL PRIMARY KEY,
    evaluation_id BIGINT NOT NULL REFERENCES annual_evaluation(id) ON DELETE CASCADE,
    criteria_id BIGINT NOT NULL REFERENCES category_criteria(id),
    head_rank INT,
    UNIQUE (evaluation_id, criteria_id)
);
