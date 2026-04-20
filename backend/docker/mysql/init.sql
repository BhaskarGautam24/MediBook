-- =============================================================
-- MediBook Platform — Database Initialization
-- Creates one database per microservice
-- =============================================================

CREATE DATABASE IF NOT EXISTS medibook_auth;
CREATE DATABASE IF NOT EXISTS medibook_provider;
CREATE DATABASE IF NOT EXISTS medibook_schedule;
CREATE DATABASE IF NOT EXISTS medibook_appointment;
CREATE DATABASE IF NOT EXISTS medibook_payment;
CREATE DATABASE IF NOT EXISTS medibook_review;
CREATE DATABASE IF NOT EXISTS medibook_notification;
CREATE DATABASE IF NOT EXISTS medibook_record;

-- Grant full privileges to root on all databases
GRANT ALL PRIVILEGES ON medibook_auth.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON medibook_provider.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON medibook_schedule.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON medibook_appointment.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON medibook_payment.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON medibook_review.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON medibook_notification.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON medibook_record.* TO 'root'@'%';

FLUSH PRIVILEGES;
