-- Portfolio Categories for Evaluation (Title-specific dimensions)
CREATE TABLE portfolio_category (
    id BIGSERIAL PRIMARY KEY,
    title_id BIGINT NOT NULL REFERENCES employee_title(id) ON DELETE CASCADE,
    category_name VARCHAR(200) NOT NULL,
    description TEXT,
    sort_order INT NOT NULL DEFAULT 0,
    is_system_default BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(title_id, category_name)
);

-- Category Rank Labels (e.g., 1=Poor, 2=Fair, 3=Good, 4=Very Good, 5=Excellent)
CREATE TABLE category_rank_label (
    id BIGSERIAL PRIMARY KEY,
    category_id BIGINT NOT NULL REFERENCES portfolio_category(id) ON DELETE CASCADE,
    rank INT NOT NULL CHECK (rank >= 1 AND rank <= 5),
    label VARCHAR(200) NOT NULL,
    description TEXT,
    UNIQUE(category_id, rank)
);

-- Category Criteria/Standards (Rubric items for each category)
CREATE TABLE category_criteria (
    id BIGSERIAL PRIMARY KEY,
    category_id BIGINT NOT NULL REFERENCES portfolio_category(id) ON DELETE CASCADE,
    criteria_name VARCHAR(300) NOT NULL,
    description TEXT,
    sort_order INT NOT NULL DEFAULT 0
);

-- Create indices for efficient querying
CREATE INDEX idx_portfolio_category_title ON portfolio_category(title_id);
CREATE INDEX idx_category_rank_label_category ON category_rank_label(category_id);
CREATE INDEX idx_category_criteria_category ON category_criteria(category_id);
