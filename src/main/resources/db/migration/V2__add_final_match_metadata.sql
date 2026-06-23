ALTER TABLE uno_games
    ADD COLUMN target_score INTEGER;

ALTER TABLE uno_games
    ADD COLUMN completion_reason VARCHAR(30) NOT NULL DEFAULT 'ROUND_CAP';

ALTER TABLE uno_games
    ADD CONSTRAINT ck_uno_games_target_score CHECK (
        target_score IS NULL OR target_score > 0
    );

ALTER TABLE uno_games
    ADD CONSTRAINT ck_uno_games_completion_reason CHECK (
        completion_reason IN ('ROUND_CAP', 'TARGET_SCORE')
    );
