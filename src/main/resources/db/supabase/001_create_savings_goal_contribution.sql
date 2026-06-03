CREATE TABLE IF NOT EXISTS savings_goal_contribution (
    id UUID PRIMARY KEY,
    goal_id UUID NOT NULL REFERENCES savings_goal(id) ON DELETE CASCADE,
    amount NUMERIC(12, 2) NOT NULL CHECK (amount > 0),
    contribution_date DATE NOT NULL,
    note TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_savings_goal_contribution_goal_id
    ON savings_goal_contribution(goal_id);

CREATE INDEX IF NOT EXISTS idx_savings_goal_contribution_date
    ON savings_goal_contribution(contribution_date DESC);
