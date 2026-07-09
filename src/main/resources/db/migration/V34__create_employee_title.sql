-- Employee Title Management for Portfolio Tracking
CREATE TABLE employee_title (
    id BIGSERIAL PRIMARY KEY,
    title_name VARCHAR(200) NOT NULL,
    department_id BIGINT REFERENCES department(id),
    is_system_default BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(title_name, department_id)
);

-- Create index for quick lookups
CREATE INDEX idx_employee_title_department ON employee_title(department_id);
CREATE INDEX idx_employee_title_system_default ON employee_title(is_system_default);
