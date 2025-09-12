-- SQLite script to recreate the accounts table with proper constraints
-- This ensures account names are unique per user, not globally unique

-- First, create a backup of the existing accounts table
CREATE TABLE accounts_backup AS SELECT * FROM accounts;

-- Drop the existing accounts table
DROP TABLE accounts;

-- Create a new accounts table with the correct structure and constraints
CREATE TABLE accounts (
    account_id INTEGER PRIMARY KEY AUTOINCREMENT,
    account_name TEXT NOT NULL,
    account_balance NUMERIC NOT NULL,
    currency_code TEXT NOT NULL,
    user_id INTEGER NOT NULL
);

-- Create a unique index on the combination of account_name and user_id
-- This ensures account names are unique per user
CREATE UNIQUE INDEX idx_account_name_user_id ON accounts (account_name, user_id);

-- Copy data back from the backup table
INSERT INTO accounts (account_id, account_name, account_balance, currency_code, user_id)
SELECT account_id, account_name, account_balance, currency_code, user_id FROM accounts_backup;

-- Drop the backup table
DROP TABLE accounts_backup;
