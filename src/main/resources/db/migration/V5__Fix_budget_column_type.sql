-- V5__Fix_budget_column_type.sql
-- Fix budget column type mismatch

-- Drop and recreate budget column with correct type
ALTER TABLE projects
DROP COLUMN IF EXISTS budget;

ALTER TABLE projects
ADD COLUMN budget DECIMAL(15,2);