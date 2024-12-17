CREATE TABLE IF NOT EXISTS CLIENT (
                               id BIGINT AUTO_INCREMENT PRIMARY KEY,
                               name VARCHAR(100) NOT NULL,
                               email VARCHAR(100) NOT NULL UNIQUE,
                               gender VARCHAR(100) NOT NULL,
                               status VARCHAR(50) NOT NULL,
                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
                           );