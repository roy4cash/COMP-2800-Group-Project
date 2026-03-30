# FAT - Financial Activity Tracker

Financial Activity Tracker (FAT) is a Java Swing desktop application for tracking expenses, monthly budgets, and investments.

The submission stack is:
- Java Swing
- MySQL
- JDBC
- JFreeChart
- Azure VM

The desktop app is the primary deliverable. The repository also includes aligned submission docs in `docs/`, a Word document generator for `FAT_User_Requirements.docx`, and a small static `web/` page that explains the project.

## Quick Start

1. Install JDK 21.
2. Install MySQL Server on the Azure VM or target machine.
3. Create a MySQL database named `fat`.
4. Run `sql/fat_schema.sql` against that database.
5. Place MySQL Connector/J in `lib/`.
   Recommended file name: `mysql-connector-j-8.4.0.jar`
6. Set the database environment variables:

```powershell
$env:FAT_DB_URL = "jdbc:mysql://localhost:3306/fat?serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true"
$env:FAT_DB_USER = "root"
$env:FAT_DB_PASSWORD = "YOUR_PASSWORD"
```

7. Compile from the repository root:

```bash
javac -cp "lib/*" -d out @sources.txt
```

8. Run the desktop app:

Windows:

```bash
java -cp "out;lib/*" app.Main
```

macOS/Linux:

```bash
java -cp "out:lib/*" app.Main
```

If the MySQL driver jar is missing, the app now reports that clearly at runtime.

## Azure VM Demo Setup

Recommended grading setup:
- Windows Azure VM with a desktop environment
- MySQL Server installed locally on the same VM
- repository copied onto the VM
- app launched manually with `javac` and `java`

The deployment steps are documented in [docs/DEPLOYMENT.md](docs/DEPLOYMENT.md).

## Documentation

- [Dependencies and Setup](docs/DEPENDENCIES.md)
- [Deployment Guide](docs/DEPLOYMENT.md)
- [User Guide](docs/USER_GUIDE.md)
- [Design and Architecture](docs/DESIGN.md)

## Implementation Notes

- The app is single-user and uses a default session mapped to `user_id = 1`.
- Expenses and budgets refresh through the Observer pattern via `ExpenseManager`.
- Dashboard and Insights are scoped to the current calendar month.
- The Investments tab stores data in the same MySQL database but refreshes through its own panel flow.
- The `investments` table is created automatically when the Investments tab is opened.
- `sources.txt` is the canonical Java source list used for compilation.

## Repository Layout

- `src/` Java source code
- `sql/fat_schema.sql` MySQL schema for `users`, `categories`, `budgets`, and `expenses`
- `lib/` runtime JAR dependencies
- `sources.txt` source list used by `javac`
- `docs/` submission-facing documentation
- `generate_ur.js` Word document generator
- `web/` static informational page

## Regenerate The Word Submission Document

```bash
npm install
npm run generate:urd
```
