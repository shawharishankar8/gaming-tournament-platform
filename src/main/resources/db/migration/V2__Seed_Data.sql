-- Insert admin user (password: admin123)
INSERT INTO users (username, email, password, role, created_at, updated_at)
VALUES (
    'admin',
    'admin@tournament.com',
    '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a',
    'ADMIN',
    NOW(),
    NOW()
);

-- Insert sample players
INSERT INTO users (username, email, password, role, created_at, updated_at) VALUES
('john_doe', 'john@example.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'PLAYER', NOW(), NOW()),
('sarah_gamer', 'sarah@example.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'PLAYER', NOW(), NOW()),
('mike_pro', 'mike@example.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'PLAYER', NOW(), NOW()),
('lisa_champ', 'lisa@example.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'PLAYER', NOW(), NOW());

-- Create player profiles linked to users
INSERT INTO players (user_id, gamer_tag, elo_rating, wins, losses, created_at, updated_at) VALUES
((SELECT id FROM users WHERE username = 'john_doe'), 'JohnTheDestroyer', 1450, 25, 15, NOW(), NOW()),
((SELECT id FROM users WHERE username = 'sarah_gamer'), 'SarahQueen', 1620, 42, 18, NOW(), NOW()),
((SELECT id FROM users WHERE username = 'mike_pro'), 'MikeThePro', 1580, 38, 22, NOW(), NOW()),
((SELECT id FROM users WHERE username = 'lisa_champ'), 'LisaChampion', 1750, 55, 12, NOW(), NOW());

-- Insert sample tournaments
INSERT INTO tournaments (name, type, max_participants, entry_fee, prize_pool, status, start_time, created_at, updated_at) VALUES
('Spring Championship 2024', 'SINGLE_ELIMINATION', 16, 25.00, 400.00, 'UPCOMING', NOW() + INTERVAL '7 days', NOW(), NOW()),
('Weekly Quick Tournament', 'SINGLE_ELIMINATION', 8, 10.00, 80.00, 'UPCOMING', NOW() + INTERVAL '2 days', NOW(), NOW()),
('Pro League Qualifiers', 'SINGLE_ELIMINATION', 32, 50.00, 1600.00, 'UPCOMING', NOW() + INTERVAL '14 days', NOW(), NOW());

-- Register players for tournaments
INSERT INTO tournament_registrations (tournament_id, player_id, payment_status, created_at, updated_at) VALUES
(1, 1, 'PAID', NOW(), NOW()),
(1, 2, 'PAID', NOW(), NOW()),
(1, 3, 'PENDING', NOW(), NOW()),
(2, 1, 'PAID', NOW(), NOW()),
(2, 4, 'PAID', NOW(), NOW());