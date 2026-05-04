CREATE TABLE IF NOT EXISTS post (
    id BIGINT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    text TEXT NOT NULL,
    tags TEXT, -- будем хранить как строку (JSON или CSV)
    likes_count INT DEFAULT 0,
    comments_count INT DEFAULT 0
);

INSERT INTO post (id, title, text, tags, likes_count, comments_count) VALUES
(1, 'First Post', 'This is my first post', '["java","spring"]', 10, 2),
(2, 'Second Post', 'Learning Spring is fun', '["spring","backend"]', 25, 5),
(3, 'Third Post', 'Microservices guide', '["microservices","architecture"]', 40, 8);
