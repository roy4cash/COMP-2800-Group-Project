# Deployment Guide

This repository is deployed manually. The primary grading target is an Azure VM that can launch a Java Swing desktop application and run MySQL locally on the same machine.

## Recommended Azure VM Model

Use a Windows Azure VM with:
- JDK 21
- MySQL Server 8.x installed locally
- a desktop session available for launching Swing applications
- this repository copied onto the VM

The app does not use Docker, Maven, Gradle, or infrastructure-as-code deployment.

## 1. Install Software On The VM

Install or verify:
- JDK 21
- MySQL Server 8.x
- MySQL Command Line Client or MySQL Workbench

## 2. Create The Database

From MySQL, create a database and a local user. Example:

```sql
CREATE DATABASE fat;
CREATE USER 'fat_user'@'localhost' IDENTIFIED BY 'YOUR_PASSWORD';
GRANT ALL PRIVILEGES ON fat.* TO 'fat_user'@'localhost';
FLUSH PRIVILEGES;
```

If you prefer to use `root` for the demo machine, that also works as long as the credentials match the app configuration.

## 3. Run The Schema

From the repository root:

```bash
mysql -u fat_user -p fat < sql/fat_schema.sql
```

This creates:
- `users`
- `categories`
- `budgets`
- `expenses`

The `investments` table is created automatically by the desktop app when the Investments tab is opened.

## 4. Add Java Dependencies

Make sure `lib/` contains:
- `mysql-connector-j-8.4.0.jar` or a compatible MySQL Connector/J JAR
- `jfreechart-1.0.19.jar`
- `jcommon-1.0.23.jar`

## 5. Configure Runtime Variables

On Windows PowerShell:

```powershell
$env:FAT_DB_URL = "jdbc:mysql://localhost:3306/fat?serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true"
$env:FAT_DB_USER = "fat_user"
$env:FAT_DB_PASSWORD = "YOUR_PASSWORD"
```

On macOS/Linux:

```bash
export FAT_DB_URL="jdbc:mysql://localhost:3306/fat?serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true"
export FAT_DB_USER="fat_user"
export FAT_DB_PASSWORD="YOUR_PASSWORD"
```

## 6. Compile The App

From the repository root:

```bash
javac -cp "lib/*" -d out @sources.txt
```

## 7. Launch The App

Windows:

```bash
java -cp "out;lib/*" app.Main
```

macOS/Linux:

```bash
java -cp "out:lib/*" app.Main
```

## 8. First-Run Behavior

On first use:
- `ExpenseManager` ensures that the default user (`user_id = 1`) exists
- the Investments tab creates the `investments` table if it is missing

## 9. TA Smoke Test Checklist

After launch, verify:
- the main window opens
- tabs render correctly
- categories appear in Add Expense
- a budget can be saved
- an expense can be added
- the Transactions tab refreshes automatically
- the Dashboard and Insights tabs update after an expense is added
- the Investments tab opens and can add or delete records without crashing

## Notes

- The Azure VM is the main demo machine, so MySQL is expected to be installed locally on that VM.
- The repository does not include an installer or packaged executable.
- The optional `web/` folder is now a static informational page only. It is not part of the runtime deployment path.
