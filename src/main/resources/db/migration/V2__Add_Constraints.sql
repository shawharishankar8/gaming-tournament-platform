-- Make user_id nullable initially for existing data, then we'll enforce it
-- This allows existing players to work while we transition
ALTER TABLE players ALTER COLUMN user_id DROP NOT NULL;

-- Add unique constraint to ensure one player per user
ALTER TABLE players ADD CONSTRAINT uk_player_user UNIQUE (user_id);

-- Add index for better performance on user lookups
CREATE INDEX IF NOT EXISTS idx_tournament_status ON tournaments(status);
CREATE INDEX IF NOT EXISTS idx_match_tournament_round ON matches(tournament_id, round_number);