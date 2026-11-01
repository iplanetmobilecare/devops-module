CREATE DATABASE IF NOT EXISTS tododb
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'root'@'%' IDENTIFIED BY 'password123';
ALTER USER 'root'@'%' IDENTIFIED BY 'password123';
GRANT ALL PRIVILEGES ON tododb.* TO 'root'@'%' WITH GRANT OPTION;
FLUSH PRIVILEGES;

USE tododb;

CREATE TABLE IF NOT EXISTS todos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    category VARCHAR(100) DEFAULT 'Personal 🌷',
    priority VARCHAR(20) DEFAULT 'MEDIUM',
    completed BOOLEAN DEFAULT FALSE,
    due_date VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO todos (title, category, priority, completed, due_date) VALUES
('Design pink & white aesthetic UI 🌸', 'Personal 🌷', 'HIGH', TRUE, 'Today'),
('Connect EC2 MySQL database 💖', 'Work 💖', 'HIGH', FALSE, 'Today'),
('Deploy WAR package on Tomcat server 🎀', 'Work 💖', 'MEDIUM', FALSE, 'Tomorrow'),
('Celebrate with strawberry cupcakes! 🧁✨', 'Celebration ✨', 'LOW', FALSE, 'Weekend');
