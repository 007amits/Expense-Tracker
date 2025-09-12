-- Drop existing index/constraint on account_name if it exists
DROP INDEX IF EXISTS accounts_account_name_idx;
DROP INDEX IF EXISTS sqlite_autoindex_accounts_1;

-- Create new composite index for account_name and user_id
CREATE UNIQUE INDEX IF NOT EXISTS accounts_name_user_idx ON accounts (account_name, user_id);
