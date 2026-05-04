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