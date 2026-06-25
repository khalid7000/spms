CREATE TABLE planning_cycle (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    start_year INT NOT NULL,
    end_year INT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE assessment_period (
    id BIGSERIAL PRIMARY KEY,
    planning_cycle_id BIGINT NOT NULL REFERENCES planning_cycle(id),
    name VARCHAR(100) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    sort_order INT NOT NULL DEFAULT 0
);

CREATE TABLE department (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE app_user (
    id BIGSERIAL PRIMARY KEY,
    fname VARCHAR(100) NOT NULL,
    lname VARCHAR(100) NOT NULL,
    email VARCHAR(200) NOT NULL UNIQUE,
    title VARCHAR(200),
    department_id BIGINT REFERENCES department(id),
    is_admin BOOLEAN NOT NULL DEFAULT false,
    active BOOLEAN NOT NULL DEFAULT true,
    password_hash VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE strategy (
    id BIGSERIAL PRIMARY KEY,
    planning_cycle_id BIGINT NOT NULL REFERENCES planning_cycle(id),
    department_id BIGINT REFERENCES department(id),
    strategy_type VARCHAR(20) NOT NULL,
    state VARCHAR(20) NOT NULL DEFAULT 'CREATION',
    title VARCHAR(300) NOT NULL,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE UNIQUE INDEX uq_strategy_cycle_dept ON strategy(planning_cycle_id, department_id) WHERE department_id IS NOT NULL;
CREATE UNIQUE INDEX uq_strategy_cycle_univ ON strategy(planning_cycle_id) WHERE department_id IS NULL;

CREATE TABLE role_assignment (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES app_user(id),
    strategy_id BIGINT NOT NULL REFERENCES strategy(id),
    role VARCHAR(20) NOT NULL,
    UNIQUE(user_id, strategy_id)
);

CREATE TABLE theme (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    planning_cycle_id BIGINT NOT NULL REFERENCES planning_cycle(id)
);

CREATE TABLE goal (
    id BIGSERIAL PRIMARY KEY,
    strategy_id BIGINT NOT NULL REFERENCES strategy(id) ON DELETE CASCADE,
    theme_id BIGINT REFERENCES theme(id),
    title VARCHAR(500) NOT NULL,
    description TEXT,
    sort_order INT NOT NULL DEFAULT 0,
    created_by BIGINT REFERENCES app_user(id),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE objective (
    id BIGSERIAL PRIMARY KEY,
    goal_id BIGINT NOT NULL REFERENCES goal(id) ON DELETE CASCADE,
    title VARCHAR(500) NOT NULL,
    description TEXT,
    sort_order INT NOT NULL DEFAULT 0,
    frozen BOOLEAN NOT NULL DEFAULT false,
    created_by BIGINT REFERENCES app_user(id),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE objective_mapping (
    id BIGSERIAL PRIMARY KEY,
    dept_objective_id BIGINT NOT NULL REFERENCES objective(id) ON DELETE CASCADE,
    university_objective_id BIGINT NOT NULL REFERENCES objective(id) ON DELETE CASCADE,
    UNIQUE(dept_objective_id, university_objective_id)
);

CREATE TABLE initiative (
    id BIGSERIAL PRIMARY KEY,
    objective_id BIGINT NOT NULL REFERENCES objective(id) ON DELETE CASCADE,
    title VARCHAR(500) NOT NULL,
    description TEXT,
    sort_order INT NOT NULL DEFAULT 0,
    created_by BIGINT REFERENCES app_user(id),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE initiative_mapping (
    id BIGSERIAL PRIMARY KEY,
    dept_initiative_id BIGINT NOT NULL UNIQUE REFERENCES initiative(id) ON DELETE CASCADE,
    university_initiative_id BIGINT NOT NULL REFERENCES initiative(id) ON DELETE CASCADE
);

CREATE TABLE measurement (
    id BIGSERIAL PRIMARY KEY,
    initiative_id BIGINT NOT NULL REFERENCES initiative(id) ON DELETE CASCADE,
    description VARCHAR(500) NOT NULL,
    unit VARCHAR(100),
    target_value DECIMAL(15,4),
    actual_value DECIMAL(15,4),
    sort_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE achievement_type (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    active BOOLEAN NOT NULL DEFAULT true
);

CREATE TABLE achievement (
    id BIGSERIAL PRIMARY KEY,
    measurement_id BIGINT NOT NULL REFERENCES measurement(id) ON DELETE CASCADE,
    title VARCHAR(500) NOT NULL,
    achievement_type_id BIGINT NOT NULL REFERENCES achievement_type(id),
    details TEXT,
    author_id BIGINT NOT NULL REFERENCES app_user(id),
    assessment_period_id BIGINT REFERENCES assessment_period(id),
    recorded_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE audit_log (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES app_user(id),
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(100),
    entity_id BIGINT,
    strategy_id BIGINT REFERENCES strategy(id),
    old_value TEXT,
    new_value TEXT,
    details TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE comment (
    id BIGSERIAL PRIMARY KEY,
    strategy_id BIGINT NOT NULL REFERENCES strategy(id) ON DELETE CASCADE,
    entity_type VARCHAR(100),
    entity_id BIGINT,
    author_id BIGINT NOT NULL REFERENCES app_user(id),
    content TEXT NOT NULL,
    parent_comment_id BIGINT REFERENCES comment(id),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

INSERT INTO achievement_type (name) VALUES ('Event'),('Publication'),('Award'),('Grant'),('Partnership');
