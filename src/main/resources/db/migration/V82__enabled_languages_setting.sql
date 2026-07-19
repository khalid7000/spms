-- Adds one row to the existing organization_setting key->string store (see
-- V72__organization_settings.sql for the table itself) so the Admin can pick which languages are
-- available to all users, from a checkbox list driven by the languages the system has an XML
-- translation file for (see spms-client/public/locales/). Defaults to English-only; the Admin
-- turns on additional languages (e.g. Arabic) via Organization Settings.
INSERT INTO organization_setting (setting_key, value, description) VALUES
    ('ENABLED_LANGUAGES', 'en',
     'Comma-separated language codes enabled for all users, from the languages the system has translations for (see Admin > Organization Settings)');
