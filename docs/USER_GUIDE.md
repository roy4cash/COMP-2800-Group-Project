# Financial Activity Tracker (FAT)
## User Guide

---

**Course Project:** COMP-2800 Software Development Project  
**Application Type:** Java Swing Desktop Application  
**Technology Stack:** Java Swing, MySQL, JDBC, JFreeChart  
**Deployment Target:** Local machine or Azure Virtual Machine  

**Prepared By:** `[Group Name / Team Members]`  
**Version:** `1.0`  
**Date:** `[Insert Submission Date]`

---

> **Screenshot Placeholder - Cover Screen**  
> Caption: *Figure 1. FAT application main window shown after successful launch.*

## Contents

- Cover Page
- 1. Introduction
- 2. System Requirements
- 3. Getting Started
- 4. Interface Overview
- 5. Feature Walkthrough by Tab
- 6. Common Tasks
- 7. Validation and Error Handling
- 8. Troubleshooting
- 9. Conclusion

## 1. Introduction

Financial Activity Tracker (FAT) is a desktop application designed to help users manage personal spending, monthly budgets, and investment holdings in one place. The system provides a simple tab-based interface built with Java Swing and stores data in a MySQL database using JDBC.

The application is intended for users who want to:

- record day-to-day expenses
- set and monitor a monthly budget
- review spending trends through charts and insights
- manage a small manual investment portfolio
- view recent transactions and category summaries from a dashboard

FAT is designed as a practical university software project, so the interface focuses on clarity, useful feedback, and easy demonstration rather than complex financial automation.

## 2. System Requirements

The following environment is recommended for running the application successfully.

| Component | Requirement |
| --- | --- |
| Operating System | Windows 10/11 recommended |
| Java | JDK 21 |
| Database | MySQL Server 8.x |
| Java Database Driver | MySQL Connector/J |
| Chart Library | JFreeChart with JCommon |
| Memory/Hardware | Standard desktop or Azure VM environment |
| Network | Not required for normal use after setup |

Required runtime files in `lib/`:

- `mysql-connector-j-8.4.0.jar`
- `jfreechart-1.0.19.jar`
- `jcommon-1.0.23.jar`

## 3. Getting Started

This section explains how to prepare the application for first use.

### 3.1 Prepare the Database

1. Install MySQL Server.
2. Create a database named `fat`.
3. Run the schema file:

```bash
mysql -u root -p fat < sql/fat_schema.sql
```

The schema creates the core application tables:

- `users`
- `categories`
- `budgets`
- `expenses`

The `investments` table is created automatically the first time the **Investments** tab is opened with a working database connection.

### 3.2 Set Database Configuration

Before launching the application, set the required environment variables.

Windows PowerShell:

```powershell
$env:FAT_DB_URL = "jdbc:mysql://localhost:3306/fat?serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true"
$env:FAT_DB_USER = "root"
$env:FAT_DB_PASSWORD = "YOUR_PASSWORD"
```

These values tell the application how to connect to MySQL at startup.

### 3.3 Compile the Project

From the project root folder, run:

```bash
javac -cp "lib/*" -d out @sources.txt
```

### 3.4 Run the Application

Windows:

```bash
java -cp "out;lib/*" app.Main
```

macOS/Linux:

```bash
java -cp "out:lib/*" app.Main
```

### 3.5 First Launch Notes

On first launch:

- the application opens to the main desktop window
- the current month is shown in the header
- expense, insights, transactions, and dashboard data are based on the current calendar month
- the app uses a default local session for a single user

If the database is missing or misconfigured, the application still opens but shows clear error messages instead of crashing.

> **Screenshot Placeholder - Startup / Home View**  
> Caption: *Figure 2. FAT after launch, showing the main header and tab navigation.*

## 4. Interface Overview

The application uses a tabbed layout so each major feature is separated into its own workspace.

### 4.1 Main Window Structure

The interface includes:

- a top header showing the application name and current month
- a tab bar for moving between major features
- a main content area that changes based on the selected tab
- inline status messages for success, warnings, and errors

### 4.2 Navigation Tabs

The current implementation includes five tabs:

- **Add Expense**: add a new expense and set the current month budget
- **Insights**: view current-month spending analytics and tips
- **Investments**: track manual investment holdings
- **Transactions**: search, sort, filter, and delete expense records
- **Dashboard**: view budget progress, charts, top categories, and recent activity

### 4.3 Feedback Style

FAT uses clear inline feedback throughout the interface:

- green messages for successful actions
- red messages for validation or database problems
- warning text when limited fallback behavior is being used
- empty-state panels when no records are available yet

## 5. Feature Walkthrough by Tab

### 5.1 Add Expense

The **Add Expense** tab is used for the two most common tasks in the system:

- recording a new expense
- setting or updating the monthly budget

### Expense Form

The expense form includes:

- **Category**
- **Description**
- **Amount ($)**
- **Date (YYYY-MM-DD)**

To add an expense:

1. Select a category.
2. Enter a short description.
3. Enter a positive amount.
4. Enter a valid date.
5. Click **Add Expense**.

When the entry is successful:

- the form is cleared
- a success message appears
- the Dashboard, Insights, and Transactions views refresh automatically

### Monthly Budget Panel

The budget panel allows the user to define the spending target for the current month.

To set a budget:

1. Enter a positive budget amount.
2. Click **Set Budget**.

When the budget is saved:

- the current budget value is shown in the panel
- dashboard alerts and summaries update automatically
- insights can compare projected spending against the budget

> **Screenshot Placeholder - Add Expense Tab**  
> Caption: *Figure 3. Add Expense tab showing the expense form on the left and monthly budget panel on the right.*

### 5.2 Insights

The **Insights** tab summarizes current-month spending using calculated metrics and a chart.

### Statistics Cards

The page includes live summary cards such as:

- top spending category
- average daily spend
- total spent this month
- number of transactions
- average transaction size
- largest expense
- projected month-end spending
- budget risk status

### Category Chart

The bar chart displays up to five categories with the highest spending totals for the current month. This helps users quickly identify where most of their money is going.

### Financial Tips

The tips section generates practical messages based on actual user data, such as:

- the category taking the largest share of spending
- average transaction size
- projected monthly total based on current pace
- whether the current month is on track, close to budget, or over budget

> **Screenshot Placeholder - Insights Tab**  
> Caption: *Figure 4. Insights tab showing current-month metrics, a category chart, and financial tips.*

### 5.3 Investments

The **Investments** tab allows the user to track holdings manually. It is intentionally separate from the expense budget workflow.

### Portfolio Summary

At the top of the page, the system shows:

- total invested
- current portfolio value
- total gain/loss
- holdings count
- best performer
- worst performer

### Add Investment Form

The form collects:

- name
- ticker
- type
- shares
- buy price
- current price
- purchase date

To add an investment:

1. Enter the holding details.
2. Click **Add Investment**.
3. Use **Refresh Portfolio** if you want to manually reload the summary and table.

### Holdings Table

The table lists all saved holdings and includes:

- total cost
- current value
- gain/loss in dollars
- gain/loss percentage
- delete action

Delete actions require confirmation before the row is removed.

> **Screenshot Placeholder - Investments Tab**  
> Caption: *Figure 5. Investments tab showing summary cards, the add form, and the holdings table.*

### 5.4 Transactions

The **Transactions** tab displays the expense history in a searchable and sortable table.

### Table Features

Users can:

- view date, category, description, and amount
- search by keyword
- filter by category
- sort columns by clicking the table headers
- delete a selected transaction with confirmation

### Table Behavior

The page also includes:

- a visible transaction count
- a filtered result count
- an empty state when there are no records
- a different empty state when filters return no matches

This tab is useful when reviewing older entries or checking whether an expense was recorded correctly.

> **Screenshot Placeholder - Transactions Tab**  
> Caption: *Figure 6. Transactions tab showing search, category filter, sortable table, and delete action.*

### 5.5 Dashboard

The **Dashboard** tab gives the user a broad summary of current-month activity in one place.

### Dashboard Sections

The page combines:

- a budget alert banner
- monthly summary cards
- spending-by-category chart
- top spending categories panel
- recent transactions panel

### Budget Alert Banner

The banner changes based on budget status:

- neutral message when no budget is set
- warning when spending reaches a higher percentage of the budget
- over-budget warning when total spending exceeds the target

### Summary and Activity Panels

The dashboard helps the user answer quick questions such as:

- How much has been spent this month?
- How much budget remains?
- Which categories are the highest?
- What were the most recent transactions?

> **Screenshot Placeholder - Dashboard Tab**  
> Caption: *Figure 7. Dashboard tab with budget status, charts, top categories, and recent transactions.*

## 6. Common Tasks

This section provides quick instructions for everyday actions.

### 6.1 Add a New Expense

1. Open **Add Expense**.
2. Choose a category.
3. Enter description, amount, and date.
4. Click **Add Expense**.
5. Confirm the success message.

### 6.2 Set the Monthly Budget

1. Open **Add Expense**.
2. In the budget panel, enter the budget amount.
3. Click **Set Budget**.
4. Review the updated budget indicators in **Dashboard** and **Insights**.

### 6.3 Find a Transaction

1. Open **Transactions**.
2. Type a keyword into the search field.
3. Optionally select a category filter.
4. Sort the table if needed.

### 6.4 Delete a Transaction

1. Open **Transactions**.
2. Select the target row.
3. Click **Delete Selected**.
4. Confirm the delete prompt.

### 6.5 Add an Investment

1. Open **Investments**.
2. Fill in the holding details.
3. Click **Add Investment**.
4. Review the updated table and summary cards.

### 6.6 Review Spending Trends

1. Open **Insights** for analytical summaries.
2. Open **Dashboard** for a broader overview with recent activity.

## 7. Validation and Error Handling

FAT includes validation and safe error handling to reduce invalid data and prevent crashes.

### 7.1 Expense Validation

The expense form checks that:

- a category is selected
- description is not blank
- description is not longer than the allowed limit
- amount is a positive number with a valid money format
- date uses `YYYY-MM-DD`
- date is not in the future

### 7.2 Budget Validation

The budget field checks that:

- the amount is present
- the amount is numeric
- the amount is greater than zero

### 7.3 Investment Validation

The investment form checks that:

- name is provided
- ticker is valid
- shares are positive
- buy price is positive
- current price is zero or greater when entered
- purchase date is valid and not in the future

### 7.4 Database Error Handling

If MySQL is unavailable or the configuration is missing, the application:

- stays open instead of crashing
- shows clear error messages
- prevents actions that are guaranteed to fail
- displays informative empty or unavailable states in affected pages

## 8. Troubleshooting

### 8.1 The Application Opens but Data Will Not Save

Check the following:

- `FAT_DB_URL` is set correctly
- `FAT_DB_USER` is correct
- `FAT_DB_PASSWORD` is correct
- MySQL Server is running
- the `fat` database exists
- `sql/fat_schema.sql` has been run

### 8.2 The App Reports a Missing Database Driver

Make sure the MySQL JDBC driver is present in `lib/`:

- `mysql-connector-j-8.4.0.jar`

Then recompile and run the project again.

### 8.3 Categories Show a Warning

If category data cannot be loaded from MySQL, the application may show built-in preview categories with a warning message. This means the interface opened, but the database connection still needs attention before normal saving can continue.

### 8.4 Transactions, Dashboard, or Insights Show No Data

Possible reasons:

- no expenses have been added for the current month
- filters are hiding matching records
- the database is not connected

### 8.5 Investments Do Not Appear

Check that:

- the database connection is working
- the Investments tab has been opened at least once
- the table was refreshed successfully

The application creates the `investments` table automatically when the Investments tab loads successfully.

### 8.6 Date Input Is Rejected

Use the format:

```text
YYYY-MM-DD
```

Example:

```text
2026-03-29
```

## 9. Conclusion

Financial Activity Tracker (FAT) provides a clear desktop interface for recording expenses, managing a budget, and monitoring a simple investment portfolio. The system combines form-based data entry, automatic updates across related tabs, and chart-based summaries to make personal financial activity easier to review.

This guide should help users install, launch, navigate, and operate the application confidently during demos, testing, and final project submission review.

> **Screenshot Placeholder - Final Summary View**  
> Caption: *Figure 8. Example final state of the FAT application after adding expenses, setting a budget, and reviewing insights.*
