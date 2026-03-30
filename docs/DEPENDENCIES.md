# Dependencies and Setup

This repository does not use Maven or Gradle. The desktop app is compiled directly with `javac` and JAR files in `lib/`.

## Required For The Desktop App

- JDK 21 recommended for compilation and runtime
- MySQL Server 8.x
- These JARs in `lib/`:
  - `mysql-connector-j-8.4.0.jar` or a compatible MySQL Connector/J version
  - `jfreechart-1.0.19.jar`
  - `jcommon-1.0.23.jar`

## Required Database Configuration

The app reads database settings from environment variables or matching Java system properties.

- `FAT_DB_URL` or `fat.db.url`
- `FAT_DB_USER` or `fat.db.user`
- `FAT_DB_PASSWORD` or `fat.db.password`

Example PowerShell setup:

```powershell
$env:FAT_DB_URL = "jdbc:mysql://localhost:3306/fat?serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true"
$env:FAT_DB_USER = "root"
$env:FAT_DB_PASSWORD = "YOUR_PASSWORD"
```

Example macOS/Linux shell setup:

```bash
export FAT_DB_URL="jdbc:mysql://localhost:3306/fat?serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true"
export FAT_DB_USER="root"
export FAT_DB_PASSWORD="YOUR_PASSWORD"
```

## Database Initialization

Run `sql/fat_schema.sql` once against the MySQL database.

The schema creates:
- `users`
- `categories`
- `budgets`
- `expenses`

The `investments` table is created automatically by `InvestmentDAO` when the Investments tab is opened.

## Compile And Run

Compile from the repository root:

```bash
javac -cp "lib/*" -d out @sources.txt
```

Run on Windows:

```bash
java -cp "out;lib/*" app.Main
```

Run on macOS/Linux:

```bash
java -cp "out:lib/*" app.Main
```

You can also pass database settings as Java properties:

```bash
java -Dfat.db.url="jdbc:mysql://localhost:3306/fat?serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true" -Dfat.db.user="root" -Dfat.db.password="YOUR_PASSWORD" -cp "out;lib/*" app.Main
```

## Optional Tooling

- Node.js 18+ and npm: only required to regenerate `FAT_User_Requirements.docx`
- A browser: only required to open the optional static project page in `web/`

Install the Node dependency used for the Word document:

```bash
npm install
```

Regenerate the Word document:

```bash
npm run generate:urd
```

## Notes

- `sources.txt` is the canonical Java source list for compilation.
- `out/` may already contain compiled classes, but submission verification should compile from `src/`.
- If the database is missing, the MySQL driver is missing, or MySQL is unreachable, the app reports clear database-related errors in the UI instead of crashing immediately.
