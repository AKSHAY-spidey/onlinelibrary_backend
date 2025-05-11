-- Update users with null active field
UPDATE users SET active = true WHERE active IS NULL;
