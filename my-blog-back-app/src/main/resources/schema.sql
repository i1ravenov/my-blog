CREATE TABLE IF NOT EXISTS post (
    id BIGINT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    text TEXT NOT NULL,
    tags TEXT, -- будем хранить как строку (JSON или CSV)
    likes_count INT DEFAULT 0,
    comments_count INT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS comment (
    id BIGSERIAL PRIMARY KEY,
    text TEXT NOT NULL,
    post_id BIGINT NOT NULL,
    CONSTRAINT fk_comment_post
        FOREIGN KEY (post_id)
        REFERENCES post(id)
        ON DELETE CASCADE
);

INSERT INTO post (id, title, text, tags, likes_count, comments_count) VALUES
(1, 'First Post', 'This is my first post', '["java","spring"]', 10, 2),
(2, 'Second Post', 'Learning Spring is fun', '["spring","backend"]', 25, 5),
(3, 'Third Post', 'Microservices guide', '["microservices","architecture"]', 40, 8);

INSERT INTO comment (id, text, post_id) VALUES
(1, 'Комментарий к посту 1', 1),
(2, 'Ещё один комментарий к посту 1', 1);
