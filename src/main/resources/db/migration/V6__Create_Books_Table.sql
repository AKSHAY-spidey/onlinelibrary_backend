-- Create books table if it doesn't exist
CREATE TABLE IF NOT EXISTS books (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(255) NOT NULL,
    isbn VARCHAR(20) UNIQUE,
    publication_date DATE,
    publisher VARCHAR(255),
    category VARCHAR(100),
    description TEXT,
    available_copies INT DEFAULT 0,
    total_copies INT DEFAULT 0,
    cover_image_url VARCHAR(500),
    language VARCHAR(50),
    pages INT
);
