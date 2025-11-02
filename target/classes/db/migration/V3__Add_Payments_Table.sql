-- Payments table
CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    tournament_id BIGINT REFERENCES tournaments(id),
    amount DECIMAL(10,2) NOT NULL,
    type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    stripe_payment_intent_id VARCHAR(255) UNIQUE,
    stripe_payment_method_id VARCHAR(255),
    description TEXT,
    failure_message TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Add payment_id to tournament_registrations
ALTER TABLE tournament_registrations
ADD COLUMN payment_id BIGINT REFERENCES payments(id);

-- Create indexes for payments
CREATE INDEX idx_payments_user_id ON payments(user_id);
CREATE INDEX idx_payments_tournament_id ON payments(tournament_id);
CREATE INDEX idx_payments_status ON payments(status);
CREATE INDEX idx_payments_stripe_intent_id ON payments(stripe_payment_intent_id);