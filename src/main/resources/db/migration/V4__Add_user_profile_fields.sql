-- Add phone and bio fields to users table
ALTER TABLE users
ADD COLUMN IF NOT EXISTS phone VARCHAR(20),
ADD COLUMN IF NOT EXISTS bio TEXT;

-- Update admin user with sample data
UPDATE users SET
    phone = '+1234567890',
    bio = 'System Administrator with full access to all features'
WHERE username = 'admin';