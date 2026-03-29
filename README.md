# FAT — Financial Activity Tracker

A Java Swing desktop application for tracking personal expenses.
Built for COMP 2800 as a group project.

---

## Tech Stack

| Layer     | Technology                  |
|-----------|-----------------------------|
| Language  | Java 11+                    |
| GUI       | Java Swing                  |
| Database  | MySQL 8                     |
| DB Access | JDBC                        |
| Charts    | JFreeChart 1.5+             |

---

## Project Structure

```
FAT/
├── src/
│   ├── app/          Main.java               — entry point
│   ├── model/        User, Category,         — plain data classes (POJOs)
│   │                 Budget, Expense
│   ├── db/           DBConnection,           — all database access (JDBC)
│   │                 ExpenseDAO, BudgetDAO,
│   │                 CategoryDAO
│   ├── observer/     Observer, Subject,      — Observer pattern
│   │                 ExpenseManager
│   ├── ui/           MainFrame,              — all Swing panels
│   │                 AddExpensePanel,
│   │                 ExpenseTablePanel,
│   │                 DashboardPanel,
│   │                 SummaryPanel,
│   │                 AlertPanel,
│   │                 ChartPanel
│   └── util/         ValidationUtils,        — helpers (no UI, no DB)
│                     DateUtils
├── sql/
│   └── fat_schema.sql                        — database setup + seed data
├── lib/                                      — place JARs here
└── README.md
```

---

## Setup

### 1. Database
```sql
-- Run this in MySQL Workbench or the mysql CLI:
SOURCE path/to/FAT/sql/fat_schema.sql;
```

### 2. Update credentials
Open `src/db/DBConnection.java` and change:
```java
private static final String DB_USER     = "root";
private static final String DB_PASSWORD = "root";
```

### 3. Add JARs to `/lib`
Download and place in `lib/`:
- `mysql-connector-j-8.x.jar`
- `jfreechart-1.5.x.jar`
- `jcommon-1.0.x.jar`

### 4. Compile
```bash
javac -cp "lib/*" -d out $(find src -name "*.java")
```

### 5. Run
```bash
java -cp "out;lib/*" app.Main          # Windows
java -cp "out:lib/*" app.Main          # Mac / Linux
```

---

## Design Patterns Used

### Observer Pattern
`ExpenseManager` is the **Subject**.
`ExpenseTablePanel`, `SummaryPanel`, `AlertPanel`, and `ChartPanel` are **Observers**.

When you add or delete an expense, `ExpenseManager.notifyObservers()` is called
and all four panels refresh themselves automatically — with no direct coupling between them.

```
User clicks "Add Expense"
    → AddExpensePanel.handleAddExpense()
        → ExpenseManager.addExpense()         (saves to MySQL)
            → notifyObservers()
                → ExpenseTablePanel.update()  (reloads table)
                → SummaryPanel.update()       (recalculates totals)
                → AlertPanel.update()         (re-checks budget %)
                → ChartPanel.update()         (redraws pie chart)
```

---

## Features

- Add and delete expenses
- Assign expenses to categories
- Set a monthly budget
- Remaining budget displayed in real time
- Warning at 80% usage; error message when over budget
- Full transaction history table
- Pie chart of spending by category
- All data persisted in MySQL
