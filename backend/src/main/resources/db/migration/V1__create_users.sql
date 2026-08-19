CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(100) NOT NULL UNIQUE CHECK ( email = lower(email) ),
    username VARCHAR(100) NOT NULL UNIQUE CHECK (username ~ '^[a-z0-9_]+$') ,
    password_hash VARCHAR(100) ,
    role VARCHAR(10) NOT NULL DEFAULT 'USER' CHECK ( role IN ('USER', 'ADMIN') ),
    avatar_url VARCHAR(255) ,
    bio VARCHAR(300),
    is_public BOOLEAN NOT NULL DEFAULT true,
    follower_count INT NOT NULL DEFAULT 0,
    following_count INT NOT NULL DEFAULT 0,
    notification_prefs JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
)