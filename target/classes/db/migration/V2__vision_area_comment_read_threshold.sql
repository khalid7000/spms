ALTER TABLE strategy
    ADD COLUMN achievement_threshold INT NOT NULL DEFAULT 3;

CREATE TABLE vision_area (
    id BIGSERIAL PRIMARY KEY,
    strategy_id BIGINT NOT NULL REFERENCES strategy(id) ON DELETE CASCADE,
    name VARCHAR(300) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    created_by BIGINT REFERENCES app_user(id),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

ALTER TABLE goal
    ADD COLUMN area_id BIGINT REFERENCES vision_area(id) ON DELETE SET NULL;

CREATE TABLE comment_read (
    id BIGSERIAL PRIMARY KEY,
    comment_id BIGINT NOT NULL REFERENCES comment(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    read_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(comment_id, user_id)
);
