package com.expense.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Utility class to fix database schema issues
 * This runs automatically when the application starts
 */
@Component
public class DatabaseSchemaFixer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseSchemaFixer.class);
    
    @Autowired
    private DataSource dataSource;
    
    @Override
    public void run(String... args) {
        fixAccountsTableConstraints();
    }
    
    /**
     * Fix the accounts table to ensure account names are unique per user, not globally unique
     */
    private void fixAccountsTableConstraints() {
        try (Connection conn = dataSource.getConnection()) {
            // Check if we need to fix the accounts table
            boolean needsFix = checkIfFixNeeded(conn);
            
            if (needsFix) {
                // Execute the fix in a transaction
                conn.setAutoCommit(false);
                
                try (Statement stmt = conn.createStatement()) {
                    // Create backup table
                    stmt.execute("CREATE TABLE IF NOT EXISTS accounts_backup AS SELECT * FROM accounts");
                    
                    // Drop existing table
                    stmt.execute("DROP TABLE IF EXISTS accounts");
                    
                    // Create new table with correct structure
                    stmt.execute("CREATE TABLE accounts (" +
                            "account_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            "account_name TEXT NOT NULL, " +
                            "account_balance NUMERIC NOT NULL, " +
                            "currency_code TEXT NOT NULL, " +
                            "user_id INTEGER NOT NULL)");
                    
                    // Create unique index for account_name + user_id
                    stmt.execute("CREATE UNIQUE INDEX idx_account_name_user_id ON accounts (account_name, user_id)");
                    
                    // Copy data back
                    stmt.execute("INSERT INTO accounts (account_id, account_name, account_balance, currency_code, user_id) " +
                            "SELECT account_id, account_name, account_balance, currency_code, user_id FROM accounts_backup");
                    
                    // Drop backup table
                    stmt.execute("DROP TABLE IF EXISTS accounts_backup");
                    
                    // Commit transaction
                    conn.commit();
                    logger.info("Successfully fixed accounts table constraints");
                } catch (SQLException e) {
                    // Rollback on error
                    conn.rollback();
                    logger.error("Error fixing accounts table constraints", e);
                } finally {
                    conn.setAutoCommit(true);
                }
            }
        } catch (SQLException e) {
            logger.error("Error checking or fixing database schema", e);
        }
    }
    
    /**
     * Check if we need to fix the accounts table constraints
     * @param conn Database connection
     * @return true if fix is needed, false otherwise
     */
    private boolean checkIfFixNeeded(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            // Check if the unique index on account_name and user_id exists
            // If it doesn't exist, we need to fix the table
            try {
                stmt.execute("SELECT 1 FROM sqlite_master WHERE type='index' AND name='idx_account_name_user_id'");
                return stmt.getResultSet().next() == false;
            } catch (SQLException e) {
                // If there's an error, assume we need to fix
                return true;
            }
        }
    }
}
