CREATE TABLE todos (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    title       TEXT      NOT NULL,
    description TEXT,
    due_date    DATETIME,
    completed   BOOLEAN   NOT NULL DEFAULT 0,
    user_id     INTEGER,
    created_at  DATETIME  NOT NULL,
    updated_at  DATETIME  NOT NULL,
    deleted_at  DATETIME,
    FOREIGN KEY(user_id) REFERENCES users(id)
);