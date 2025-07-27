-- VoidlightEvent Database Setup Script
-- Run this script to create the database and user for VoidlightEvent plugin

-- Create the database
CREATE DATABASE IF NOT EXISTS voidlight_event;

-- Create the user (change 'your_password' to a secure password)
CREATE USER IF NOT EXISTS 'voidlight'@'localhost' IDENTIFIED BY 'your_password';

-- Grant all privileges on the voidlight_event database to the user
GRANT ALL PRIVILEGES ON voidlight_event.* TO 'voidlight'@'localhost';

-- Apply the changes
FLUSH PRIVILEGES;

-- Verify the setup (optional)
SHOW DATABASES;
SELECT User, Host FROM mysql.user WHERE User = 'voidlight';

-- Note: The plugin will automatically create the necessary tables when it starts
-- You only need to run this script to set up the database and user

-- Example connection test (you can run this after the plugin starts):
-- USE voidlight_event;
-- SHOW TABLES;
-- DESCRIBE matches;