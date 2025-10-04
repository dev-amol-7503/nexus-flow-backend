-- V2__Insert_default_roles.sql
INSERT INTO roles (name, description) VALUES
('ROLE_ADMIN', 'System Administrator'),
('ROLE_PROJECT_MANAGER', 'Project Manager'),
('ROLE_TEAM_MEMBER', 'Team Member')
ON CONFLICT (name) DO NOTHING;