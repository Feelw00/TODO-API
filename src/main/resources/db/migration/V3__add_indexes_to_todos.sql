CREATE INDEX IF NOT EXISTS idx_todos_title ON todos (title);
CREATE INDEX IF NOT EXISTS idx_todos_completed ON todos (completed);
CREATE INDEX IF NOT EXISTS idx_todos_due_date ON todos (due_date);
