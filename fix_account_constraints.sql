-- SQLite doesn't support ALTER TABLE DROP CONSTRAINT
-- We need to recreate the table with the correct constraints

-- First, create a temporary table with the correct structure
CREATE TABLE accounts_new (
    account_id INTEGER PRIMARY KEY AUTOINCREMENT,
    account_name TEXT NOT NULL,
    account_balance NUMERIC NOT NULL,
    currency_code TEXT NOT NULL,
    user_id INTEGER NOT NULL
);

-- Create the composite unique index on the new table
CREATE UNIQUE INDEX accounts_name_user_idx ON accounts_new (account_name, user_id);

-- Copy data from the old table to the new one
INSERT INTO accounts_new (account_id, account_name, account_balance, currency_code, user_id)
SELECT account_id, account_name, account_balance, currency_code, user_id FROM accounts;

-- Drop the old table
DROP TABLE accounts;

-- Rename the new table to the original name
ALTER TABLE accounts_new RENAME TO accounts;
