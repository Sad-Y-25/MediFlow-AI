CREATE DATABASE mediflow;
USE mediflow;

CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(120) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL, -- On stockera ici le mot de passe haché
    role VARCHAR(30) NOT NULL,
    enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insertion d'un utilisateur test (le mot de passe devra être haché plus tard)
INSERT INTO users (full_name, email, password, role) 
VALUES ('Youssef Admin', 'admin@mediflow.com', 'admin123', 'ADMIN');


USE mediflow;

CREATE TABLE IF NOT EXISTS tickets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_name VARCHAR(255) NOT NULL,
    reason VARCHAR(255),
    urgency_level INT NOT NULL,
    status VARCHAR(50) DEFAULT 'WAITING',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO tickets (patient_name, reason, urgency_level, status) VALUES
('Zaid Test', 'Contrôle de routine', 1, 'WAITING'),
('Amine Rahali', 'Douleur intense genou', 3, 'WAITING'),
('Sara Mansouri', 'Difficultés respiratoires', 5, 'WAITING'),
('Karim Bennani', 'Fièvre et toux', 2, 'WAITING');


UPDATE tickets SET created_at = NOW() WHERE created_at IS NULL;

USE mediflow;

-- 1. Création de la table 'patients' (Section 14.2)
CREATE TABLE IF NOT EXISTS patients (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(120) NOT NULL,
    email VARCHAR(150) NOT NULL,
    phone VARCHAR(30),
    age INT,
    gender VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Création de la table 'medical_services' (Section 14.4)
CREATE TABLE IF NOT EXISTS medical_services (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    average_consultation_time INT DEFAULT 10,
    active BOOLEAN DEFAULT TRUE
);

-- Insertion de quelques services de test selon le SRS
INSERT INTO medical_services (name, average_consultation_time) VALUES
('Médecine Générale', 7),
('Cardiologie', 10),
('Dentisterie', 12);

-- 3. Création de la table 'queue_tickets' avec Clés Étrangères (Section 14.3)
CREATE TABLE IF NOT EXISTS queue_tickets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ticket_number VARCHAR(20) UNIQUE NOT NULL,
    patient_id BIGINT,
    service_id BIGINT,
    doctor_id BIGINT NULL,
    urgency_level VARCHAR(30) NOT NULL,
    has_appointment BOOLEAN DEFAULT FALSE,
    priority_score INT DEFAULT 0,
    position_number INT DEFAULT 0,
    estimated_waiting_time INT DEFAULT 0,
    status VARCHAR(40) DEFAULT 'WAITING',
    initial_email_sent BOOLEAN DEFAULT FALSE,
    reminder_email_sent BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    called_at TIMESTAMP NULL,
    consultation_started_at TIMESTAMP NULL,
    completed_at TIMESTAMP NULL,

    FOREIGN KEY (patient_id) REFERENCES patients(id),
    FOREIGN KEY (service_id) REFERENCES medical_services(id),
    FOREIGN KEY (doctor_id) REFERENCES users(id)
);