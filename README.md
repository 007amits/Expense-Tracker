# Expense Tracker - Spend Analyzer Application

## Overview
Expense Tracker is a comprehensive personal finance management application that helps users track their expenses, income, and account balances. The application provides a secure, multi-user environment where each user can only access their own financial data.

## Getting Started

### Prerequisites
- Java 17 or higher
- Maven 3.6 or higher
- Node.js 22.16.0

### Installation
1. Clone the repository
2. Navigate to the project directory
3. Run `mvn clean install` to build the application
4. Run `mvn spring-boot:run` to start the application
5. Access the application at `http://localhost:8080`
6. Do not delete any *.db, *.db.backup or *.sql file as these are empty SQLite database files

### Initial Setup
1. Register a new user account using Sign Up
2. Sign In with registered User
3. Add Expenses (Default Categories - Grocery, Food, Travel, Daily Needs)
4. Add Income (Default Accounts - Salary, Investment)
5. Start exploring other features such as 
	* Analytics (Visual representation of expenses)
	* Add new category (Expenses -> Add New Expense -> Category -> Add New)
	* Add new account (Accounts -> Add Account)
	* Dashboard (Overall view of Expenses and Income)
