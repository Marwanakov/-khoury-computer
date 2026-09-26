ALTER TABLE users
    ADD COLUMN auth_provider VARCHAR(20),
    ADD COLUMN provider_subject VARCHAR(255);

UPDATE users
SET auth_provider = 'LOCAL'
WHERE auth_provider IS NULL;

ALTER TABLE users
    ALTER COLUMN auth_provider SET NOT NULL,
    ALTER COLUMN password_hash DROP NOT NULL,
    ALTER COLUMN phone_number DROP NOT NULL,
    ALTER COLUMN address_city DROP NOT NULL,
    ALTER COLUMN address_street DROP NOT NULL,
    ALTER COLUMN address_details DROP NOT NULL;

ALTER TABLE users
    ADD CONSTRAINT users_auth_provider_check
        CHECK (auth_provider IN ('LOCAL', 'GOOGLE')),

    ADD CONSTRAINT users_authentication_details_check
        CHECK (
            (
                auth_provider = 'LOCAL'
                AND password_hash IS NOT NULL
                AND provider_subject IS NULL
            )
            OR
            (
                auth_provider = 'GOOGLE'
                AND password_hash IS NULL
                AND provider_subject IS NOT NULL
            )
        ),

    ADD CONSTRAINT users_admin_auth_provider_check
        CHECK (
            role <> 'ADMIN'
            OR auth_provider = 'LOCAL'
        );

CREATE UNIQUE INDEX users_auth_provider_subject_unique
    ON users (auth_provider, provider_subject)
    WHERE provider_subject IS NOT NULL;