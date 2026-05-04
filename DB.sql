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