#!/bin/bash
set -e

echo "=== Setting up MySQL on Ubuntu EC2 ==="

sudo apt update
sudo apt install -y mysql-server

CNF_FILE="/etc/mysql/mysql.conf.d/mysqld.cnf"
if [ -f "$CNF_FILE" ]; then
    sudo sed -i 's/^bind-address\s*=.*/bind-address = 0.0.0.0/' "$CNF_FILE"
    sudo sed -i 's/^mysqlx-bind-address\s*=.*/mysqlx-bind-address = 0.0.0.0/' "$CNF_FILE"
fi

sudo systemctl restart mysql
sudo systemctl enable mysql

sudo mysql <<EOF
CREATE DATABASE IF NOT EXISTS tododb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
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
EOF

echo "=== MySQL setup completed successfully ==="

