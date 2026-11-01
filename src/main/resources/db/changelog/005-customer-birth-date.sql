--liquibase formatted sql

--changeset goldapple:005-customer-birth-date splitStatements:false
DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'customers'
          AND column_name = 'date of birth'
          AND data_type LIKE 'timestamp%'
    ) THEN
        ALTER TABLE customers
            ALTER COLUMN "date of birth" TYPE date USING "date of birth"::date;
    ELSIF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'customers'
          AND column_name = 'date of birth'
    ) THEN
        ALTER TABLE customers ADD COLUMN "date of birth" date;
    END IF;
END $$;
--rollback ALTER TABLE customers DROP COLUMN "date of birth";
