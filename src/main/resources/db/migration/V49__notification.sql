-- In-app notifications (no email/SMTP infra exists in this app) for the Annual Evaluation
-- workflow: submitted -> head, ready-for-signature / edited-after-submit -> employee,
-- signed/refused -> the other party.
CREATE TABLE notification (
    id BIGSERIAL PRIMARY KEY,
    recipient_id BIGINT NOT NULL REFERENCES app_user(id),
    message VARCHAR(500) NOT NULL,
    evaluation_id BIGINT,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_notification_recipient ON notification(recipient_id, is_read);
