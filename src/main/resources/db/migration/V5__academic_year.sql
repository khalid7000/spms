CREATE TABLE academic_year (
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(50)  NOT NULL UNIQUE,
    start_date DATE,
    end_date   DATE,
    closed     BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- academic_year_id links an initiative copy to the year it belongs to (NULL = base/template)
-- source_initiative_id points back to the base initiative it was copied from
ALTER TABLE initiative
    ADD COLUMN academic_year_id    BIGINT REFERENCES academic_year(id) ON DELETE CASCADE,
    ADD COLUMN source_initiative_id BIGINT REFERENCES initiative(id)    ON DELETE SET NULL;

-- same pattern for measurements
ALTER TABLE measurement
    ADD COLUMN academic_year_id     BIGINT REFERENCES academic_year(id) ON DELETE CASCADE,
    ADD COLUMN source_measurement_id BIGINT REFERENCES measurement(id)  ON DELETE SET NULL;
