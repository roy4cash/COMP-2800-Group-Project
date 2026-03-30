# Financial Activity Tracker (FAT)

Financial Activity Tracker (FAT) is a Java Swing desktop application for recording expenses, setting a monthly budget, reviewing transactions, and viewing spending summaries. The project is designed as a COMP 2800 university software development submission and runs against a MySQL database through JDBC.

The desktop application is the primary deliverable in this repository.

## Project Description

FAT helps a user manage personal financial activity in one desktop interface. The application supports:

- adding expense records
- setting and updating a monthly budget
- reviewing transaction history
- viewing current-month charts and insights
- tracking simple investment holdings

The project is intended to be easy to run, easy to explain, and suitable for live grading on an Azure Virtual Machine.

## Features

- **Add Expense**
  - Add an expense with category, description, amount, and date
  - Validate user input before saving
  - Show inline success and error feedback

- **Dashboard / Insights**
  - View current-month totals and category summaries
  - See chart-based spending breakdowns using JFreeChart
  - Review recent transactions and top spending categories

- **Transactions**
  - View saved expense records in a table
  - Search, filter, sort, and delete transactions
  - Use confirmation before deletion

- **Budget Tracking**
  - Set a monthly budget
  - Monitor spending against the budget
  - Display warning and over-budget states

- **Investments**
  - Add and review manual investment holdings
  - View gain/loss summaries
  - Refresh and delete holdings

## Tech Stack

- **Java (Swing)** for the desktop user interface
- **MySQL** for persistent data storage
- **JDBC** for database access
- **JFreeChart** for charts and visual summaries
- **Azure Virtual Machine** for hosting and grading/demo use

## Project Structure

- `src/`
  - Java source code for the application
  - Includes UI, model, database, observer, and utility packages

- `lib/`
  - External JAR dependencies
  - Includes MySQL Connector/J and chart libraries

- `out/`
  - Compiled Java class files used to run the application

- `sql/`
  - Database schema scripts
  - `fat_schema.sql` creates the core MySQL tables used by the app

- `docs/`
  - Submission-facing documentation

- `sources.txt`
  - Canonical source list used for compilation

## Quick Start

### For Graders

**The application runs directly on the VM with no additional installation or configuration required.**

Assuming the VM has already been set up for grading:

1. Connect to the Azure VM using Remote Desktop.
2. Open the project folder.
3. Run the application from the repository root:

```bash
java -cp "out:lib/*" app.Main
```

If you are using Windows PowerShell directly and need the Windows classpath separator, use:

```powershell
java -cp "out;lib/*" app.Main
```

## Database Configuration

The application reads database settings from environment variables at runtime.

Required environment variables:

- `FAT_DB_URL`
- `FAT_DB_USER`
- `FAT_DB_PASSWORD`

Example:

```powershell
$env:FAT_DB_URL = "jdbc:mysql://localhost:3306/fat?serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true"
$env:FAT_DB_USER = "fat_user"
$env:FAT_DB_PASSWORD = "YOUR_PASSWORD"
```

These values tell the app how to connect to the local MySQL database on the VM.

## Azure VM Hosting

This project is intended to run on an Azure Virtual Machine with:

- Java installed
- MySQL installed locally on the same VM
- the FAT project folder already copied onto the VM

To access the application:

1. Open **Remote Desktop Connection (RDP)**.
2. Connect to the submitted Azure VM address.
3. Sign in using the VM credentials provided in the submitted report.
4. Open the FAT project folder and launch the app.

For security reasons, passwords are **not** included in this README. VM credentials are documented in the submitted report.

## Usage Instructions

After the application opens:

1. Go to **Add Expense** to enter expenses and set the monthly budget.
2. Open **Dashboard** to review budget progress, charts, and recent activity.
3. Open **Insights** to view category summaries and current-month spending metrics.
4. Open **Transactions** to search, filter, sort, or delete expense records.
5. Open **Investments** to add and manage manual holdings.

## Documentation

Additional submission documents are available in `docs/`:

- [Dependencies and Setup](docs/DEPENDENCIES.md)
- [Deployment Guide](docs/DEPLOYMENT.md)
- [User Guide](docs/USER_GUIDE.md)
- [Design and Architecture](docs/DESIGN.md)

## Implementation Notes

- The app is currently a single-user desktop application using a default session mapped to `user_id = 1`.
- Expense and budget updates use the Observer Pattern through `ExpenseManager`.
- Dashboard and Insights are based on the current calendar month.
- Investment data is stored in the same MySQL database but follows its own refresh flow.
- The `investments` table is created automatically when the Investments tab is opened.

## Troubleshooting

### Database Not Connecting

If the application opens but cannot load or save data:

1. Confirm that MySQL is running on the VM.
2. Confirm that the `fat` database exists.
3. Confirm that `sql/fat_schema.sql` has already been run.
4. Check the environment variables:
   - `FAT_DB_URL`
   - `FAT_DB_USER`
   - `FAT_DB_PASSWORD`
5. Confirm that the MySQL JDBC driver JAR is present in `lib/`.

If the database configuration is missing, the app will show an error message instead of crashing.

## Notes for Graders

- The application runs directly on the VM with no additional installation or configuration required.
- The project is a Java Swing desktop application backed by MySQL through JDBC.
- The app is already structured for demo use on the submitted Azure VM.
- Credentials and access details for the VM are included in the submitted report, not in this repository.

## Optional Document Generation

If needed, the Word-based submission documents can be regenerated with:

```bash
npm run generate:urd
npm run generate:submission-docs
```
