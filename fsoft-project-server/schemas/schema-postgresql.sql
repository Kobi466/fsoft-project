CREATE TABLE IF NOT EXISTS spring_ai_chat_memory (
    conversation_id VARCHAR(255) NOT NULL,
    sequence_id INT NOT NULL,
    type VARCHAR(50) NOT NULL,
    content TEXT NOT NULL,
    "timestamp" TIMESTAMP NOT NULL,
    PRIMARY KEY (conversation_id, sequence_id)
);
