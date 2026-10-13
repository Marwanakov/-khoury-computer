ALTER TABLE hero_slides
    ADD COLUMN IF NOT EXISTS linked_category_id BIGINT;

ALTER TABLE hero_slides
    ADD CONSTRAINT fk_hero_slides_linked_category
        FOREIGN KEY (linked_category_id)
        REFERENCES categories(id)
        ON DELETE SET NULL;

/*
 * V22 allowed PostgreSQL to generate the check-constraint name.
 * Find and remove that constraint without depending on its generated name.
 */
DO $$
DECLARE
    existing_constraint_name TEXT;
BEGIN
    SELECT constraint_definition.conname
    INTO existing_constraint_name
    FROM pg_constraint constraint_definition
    JOIN pg_class related_table
        ON related_table.oid =
           constraint_definition.conrelid
    JOIN pg_namespace related_schema
        ON related_schema.oid =
           related_table.relnamespace
    WHERE related_table.relname = 'hero_slides'
      AND related_schema.nspname = current_schema()
      AND constraint_definition.contype = 'c'
      AND pg_get_constraintdef(
              constraint_definition.oid
          ) ILIKE '%link_type%'
    LIMIT 1;

    IF existing_constraint_name IS NOT NULL THEN
        EXECUTE format(
            'ALTER TABLE hero_slides DROP CONSTRAINT %I',
            existing_constraint_name
        );
    END IF;
END
$$;

ALTER TABLE hero_slides
    ADD CONSTRAINT chk_hero_slides_link_type
        CHECK (
            link_type IN (
                'NONE',
                'PRODUCT',
                'CATEGORY',
                'PRODUCTS',
                'DEALS',
                'NEW_ARRIVALS',
                'BEST_SELLERS',
                'CONTACT'
            )
        );

CREATE INDEX IF NOT EXISTS
    idx_hero_slides_linked_category_id
    ON hero_slides(linked_category_id);