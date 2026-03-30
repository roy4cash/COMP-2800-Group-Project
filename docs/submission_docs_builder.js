const {
  AlignmentType,
  BorderStyle,
  Document,
  HeadingLevel,
  Packer,
  Paragraph,
  Table,
  TableCell,
  TableRow,
  TextRun,
  WidthType,
} = require("docx");
const fs = require("fs");

const border = { style: BorderStyle.SINGLE, size: 1, color: "D1D5DB" };
const borders = { top: border, bottom: border, left: border, right: border };

function formatDate(date) {
  return new Intl.DateTimeFormat("en-CA", {
    year: "numeric",
    month: "long",
    day: "numeric",
  }).format(date);
}

function title(text, options = {}) {
  return new Paragraph({
    alignment: options.alignment || AlignmentType.CENTER,
    spacing: { before: options.before || 120, after: options.after || 120 },
    pageBreakBefore: !!options.pageBreakBefore,
    children: [
      new TextRun({
        text,
        bold: options.bold !== false,
        size: options.size || 30,
      }),
    ],
  });
}

function heading(level, text, options = {}) {
  return new Paragraph({
    heading: level,
    pageBreakBefore: !!options.pageBreakBefore,
    spacing: { before: options.before || 260, after: options.after || 120 },
    children: [
      new TextRun({
        text,
        bold: true,
        size: options.size || 26,
      }),
    ],
  });
}

function paragraph(text, options = {}) {
  return new Paragraph({
    alignment: options.alignment || AlignmentType.LEFT,
    spacing: { before: options.before || 80, after: options.after || 100 },
    children: [
      new TextRun({
        text,
        bold: !!options.bold,
        italics: !!options.italics,
        size: options.size || 22,
      }),
    ],
  });
}

function bullet(text) {
  return new Paragraph({
    bullet: { level: 0 },
    spacing: { before: 40, after: 40 },
    children: [new TextRun({ text, size: 22 })],
  });
}

function step(number, text) {
  return paragraph(number + ". " + text);
}

function placeholder(titleText, captionText) {
  return [
    paragraph("[Insert Screenshot: " + titleText + "]", {
      italics: true,
      alignment: AlignmentType.CENTER,
      before: 140,
      after: 40,
    }),
    paragraph("Caption: " + captionText, {
      italics: true,
      alignment: AlignmentType.CENTER,
      before: 0,
      after: 140,
      size: 20,
    }),
  ];
}

function cell(text, options = {}) {
  return new TableCell({
    borders,
    width: options.width ? { size: options.width, type: WidthType.DXA } : undefined,
    margins: { top: 90, bottom: 90, left: 120, right: 120 },
    children: [
      new Paragraph({
        children: [
          new TextRun({
            text: String(text),
            bold: !!options.bold,
            size: options.size || 20,
          }),
        ],
      }),
    ],
  });
}

function table(headers, rows, widths) {
  return new Table({
    width: { size: 9360, type: WidthType.DXA },
    columnWidths: widths,
    rows: [
      new TableRow({
        tableHeader: true,
        children: headers.map((header, index) =>
          cell(header, { bold: true, width: widths[index] })
        ),
      }),
      ...rows.map((row) =>
        new TableRow({
          children: row.map((value, index) => cell(value, { width: widths[index] })),
        })
      ),
    ],
  });
}

function coverPage(todayText) {
  return [
    title("Financial Activity Tracker (FAT)", { size: 36, before: 500, after: 120 }),
    title("Submission Documents", { size: 26, before: 0, after: 220, bold: false }),
    title("User Requirements and Analysis, Design, Deployment, and User Guide", {
      size: 22,
      before: 0,
      after: 260,
      bold: false,
    }),
    table(
      ["Field", "Value"],
      [
        ["Course", "COMP-2800 Software Development Project"],
        ["Primary Stack", "Java Swing, MySQL, JDBC, JFreeChart"],
        ["Deployment Target", "Windows Azure Virtual Machine with local MySQL"],
        ["Prepared By", "[Insert Team Members]"],
        ["Date", todayText],
      ],
      [2600, 6760]
    ),
    paragraph("This file combines the four written submission documents into one Word document for easy review and copy-paste use.", {
      alignment: AlignmentType.CENTER,
      before: 220,
      after: 140,
    }),
  ];
}

function requirementsDoc(todayText) {
  const functionalRows = [
    ["FR-01", "Add Expense", "The user can add an expense with category, description, amount, and date."],
    ["FR-02", "Set Budget", "The user can create or update the budget for the current month."],
    ["FR-03", "View Insights", "The system displays current-month summaries, charts, and financial tips."],
    ["FR-04", "View Transactions", "The system shows a table of saved expense records."],
    ["FR-05", "Search and Filter", "The user can search transactions and filter them by category."],
    ["FR-06", "Sort Transactions", "The user can sort expense records by table columns."],
    ["FR-07", "Delete Expense", "The user can delete a selected expense after confirmation."],
    ["FR-08", "Track Investments", "The user can add, refresh, and delete investment holdings."],
    ["FR-09", "Dashboard Overview", "The dashboard shows budget progress, charts, recent transactions, and category summaries."],
    ["FR-10", "Validation and Feedback", "The system validates user input and displays success and error messages."],
  ];

  const nonFunctionalRows = [
    ["NFR-01", "Usability", "The application should be easy to understand for a first-time desktop user."],
    ["NFR-02", "Reliability", "The application should remain open and show readable feedback when the database is unavailable."],
    ["NFR-03", "Maintainability", "The codebase should be organized into clear packages and classes that are easy to explain."],
    ["NFR-04", "Deployability", "The app should run manually on an Azure VM with Java and MySQL installed locally."],
    ["NFR-05", "Consistency", "The UI should use consistent cards, buttons, tables, and validation behavior across tabs."],
  ];

  return [
    title("User Requirements and Analysis Document", { size: 30, pageBreakBefore: true }),
    paragraph("Financial Activity Tracker (FAT)", {
      alignment: AlignmentType.CENTER,
      bold: true,
      before: 0,
      after: 60,
    }),
    paragraph("Date: " + todayText, {
      alignment: AlignmentType.CENTER,
      before: 0,
      after: 220,
    }),
    heading(HeadingLevel.HEADING_1, "1. Introduction"),
    paragraph("Financial Activity Tracker (FAT) is a Java Swing desktop application that helps users manage personal expenses, set a monthly budget, review spending trends, and track a simple investment portfolio. The system uses MySQL as the database, JDBC for data access, and JFreeChart for visual summaries. The purpose of this document is to define the user requirements, identify the people who will interact with the system, and analyze the practical constraints that shaped the final implementation."),
    paragraph("The project was designed to feel like a serious university group submission while remaining simple enough to explain clearly. Rather than focusing on advanced financial automation, the application focuses on the core user needs of entering data, reviewing monthly activity, and understanding spending behavior through a clean desktop interface."),
    heading(HeadingLevel.HEADING_1, "2. Problem Statement"),
    paragraph("Many users do not have a simple way to review how much they are spending each month or which categories are using most of their money. Paper notes and spreadsheets can work, but they usually lack validation, visual summaries, and consistent organization. This makes it harder for users to notice patterns, control their budget, or review recent financial activity quickly."),
    paragraph("FAT solves this problem by providing a local desktop application where the user can record expenses, set a spending target, inspect current-month summaries, and manage manual investment holdings. The project also solves an academic problem by demonstrating layered design, database persistence, validation, UI feedback, and design patterns in one explainable submission."),
    heading(HeadingLevel.HEADING_1, "3. Users and Stakeholders"),
    paragraph("The primary user is someone who wants to track expenses and budget activity on a personal computer. A student or first-time finance tracker is a typical target user because the interface is simple and task-focused. The current implementation assumes a single-user local workflow instead of a multi-user environment."),
    paragraph("The instructor and teaching assistants are also important stakeholders because they need to evaluate the project as a software engineering submission. For that reason, the application must be easy to launch, easy to demonstrate, and supported by accurate documentation. The student development team is another stakeholder because the architecture needs to remain understandable enough to present in a demo or written report."),
    heading(HeadingLevel.HEADING_1, "4. Functional Requirements"),
    paragraph("The table below summarizes the main functional requirements supported by the final implementation."),
    table(["ID", "Requirement", "Description"], functionalRows, [1200, 2200, 5960]),
    heading(HeadingLevel.HEADING_1, "5. Non-Functional Requirements"),
    paragraph("In addition to the core features, FAT is expected to satisfy several non-functional requirements related to usability, reliability, maintainability, and deployment."),
    table(["ID", "Area", "Description"], nonFunctionalRows, [1200, 1800, 6360]),
    heading(HeadingLevel.HEADING_1, "6. Scope and Constraints Analysis"),
    paragraph("The scope of the project was intentionally kept practical. FAT is a Java Swing desktop application that connects directly to MySQL through JDBC. There is no web backend or cloud service layer in the runtime path. This choice keeps the system easier to deploy, easier to explain, and more appropriate for a second-year university project."),
    paragraph("The current implementation is single-user and uses a default user session. Authentication, multi-user permissions, and online synchronization are outside the final scope. This was a conscious decision so the team could focus on budget logic, expense tracking, dashboard behavior, transaction management, and investment support without adding unnecessary complexity."),
    paragraph("Another design constraint is that investment tracking is separate from expense and budget calculations. The investments module uses the same MySQL database but refreshes through its own panel flow instead of the expense observer path. This separation keeps the feature substantial enough for the project while preserving clarity in the architecture."),
    heading(HeadingLevel.HEADING_1, "7. Risks and Mitigation"),
    paragraph("A major risk in this type of project is database misconfiguration. If the MySQL server is not running, if the schema was not loaded, or if the environment variables are missing, the application could fail to save data. The current implementation reduces this risk by opening the UI safely, showing readable error messages, and preventing guaranteed-to-fail actions when the database is unavailable."),
    paragraph("Another risk is invalid input. Incorrect amounts, blank descriptions, invalid dates, or invalid investment values can lead to unreliable data or SQL failures. FAT addresses this through validation rules before save actions and through visible success and error feedback after user actions."),
    paragraph("A final risk is live-demo instability. University projects are often judged interactively, so the system must not crash during normal use. This risk is reduced through confirmation dialogs, empty-state handling, database error messaging, and automatic observer refresh behavior after successful expense and budget updates."),
    heading(HeadingLevel.HEADING_1, "8. Conclusion"),
    paragraph("The requirements and analysis for FAT show that the project is centered on realistic and useful functionality: expense tracking, budget management, transaction review, current-month insights, and manual investment tracking. The final implementation stays close to these goals and presents them in a form that is practical for users and appropriate for academic evaluation."),
    paragraph("Overall, FAT provides enough scope to feel like a complete group project while remaining focused, understandable, and technically aligned with course expectations."),
  ];
}

function designDoc(todayText) {
  const architectureRows = [
    ["Presentation Layer", "Swing UI panels in src/ui and the main frame in MainFrame"],
    ["Shared Business Logic", "ExpenseManager coordinates expense and budget operations"],
    ["Observer Layer", "Dashboard-related views, Insights, and Transactions refresh after updates"],
    ["DAO Layer", "ExpenseDAO, BudgetDAO, CategoryDAO, and InvestmentDAO perform SQL work"],
    ["Database Layer", "MySQL stores users, categories, budgets, expenses, and investments"],
  ];

  const packageRows = [
    ["src/app", "Application startup and entry point"],
    ["src/model", "Expense, Budget, Category, Investment, and User data classes"],
    ["src/db", "JDBC connection and DAO classes"],
    ["src/observer", "Observer interfaces and ExpenseManager"],
    ["src/ui", "Swing panels, tables, cards, and main frame"],
    ["src/util", "Validation, UI styling, placeholders, and helper utilities"],
  ];

  const schemaRows = [
    ["users", "Stores the default local user record"],
    ["categories", "Stores expense categories used by the expense form"],
    ["budgets", "Stores current-month budget values by month and year"],
    ["expenses", "Stores expense records with category, amount, description, and date"],
    ["investments", "Stores manual investment holdings and is created when the Investments tab is opened"],
  ];

  return [
    title("Design Document", { size: 30, pageBreakBefore: true }),
    paragraph("Financial Activity Tracker (FAT)", {
      alignment: AlignmentType.CENTER,
      bold: true,
      before: 0,
      after: 60,
    }),
    paragraph("Date: " + todayText, {
      alignment: AlignmentType.CENTER,
      before: 0,
      after: 220,
    }),
    heading(HeadingLevel.HEADING_1, "1. Design Goals"),
    paragraph("The main design goal of FAT was to create a desktop application that feels organized and substantial without becoming overly complicated. The application needed to demonstrate clear software engineering structure, including a user interface layer, a data access layer, runtime validation, database persistence, and a design pattern that supports automatic refresh behavior."),
    paragraph("To stay appropriate for a university project, the system avoids unnecessary frameworks and uses a direct architecture. Java Swing provides the interface, MySQL stores data, JDBC handles communication with the database, and JFreeChart provides charts for analytics. This design keeps the application explainable while still showing multiple layers of functionality."),
    heading(HeadingLevel.HEADING_1, "2. High-Level Architecture"),
    paragraph("The system uses a layered desktop architecture. The UI layer collects user input and displays results. Shared logic for expenses and budgets is coordinated by ExpenseManager, which acts as the main subject in the Observer Pattern. DAO classes in the database layer perform SQL operations using JDBC. The database stores the persistent state of the application."),
    table(["Layer", "Current implementation"], architectureRows, [2400, 6960]),
    paragraph("The Investments module is implemented as a parallel flow. Instead of using ExpenseManager, InvestmentPanel communicates directly with InvestmentDAO. This was chosen to keep the architecture simple while still adding a meaningful independent feature area to the project."),
    heading(HeadingLevel.HEADING_1, "3. Package Structure"),
    paragraph("The codebase is organized into a small number of focused packages so that responsibilities remain clear. This structure makes the project easier to navigate and easier to explain during a presentation or demo."),
    table(["Package", "Purpose"], packageRows, [2200, 7160]),
    heading(HeadingLevel.HEADING_1, "4. Observer Pattern Design"),
    paragraph("The Observer Pattern is used for the expense and budget flow. When the user adds or deletes an expense or updates the monthly budget, ExpenseManager notifies the registered observer panels. This allows Dashboard, Insights, and Transactions to update automatically after successful changes."),
    paragraph("This design improves separation of concerns. The Add Expense tab does not need to know how the Dashboard or Insights pages repaint themselves. It only needs to trigger the manager operation. The observer-based refresh flow makes the application feel more responsive and more complete from the user perspective."),
    heading(HeadingLevel.HEADING_1, "5. DAO Pattern Design"),
    paragraph("The database layer uses DAO classes so SQL logic stays out of the Swing panels. ExpenseDAO handles expense records, BudgetDAO handles monthly budgets, CategoryDAO loads categories, and InvestmentDAO manages investment holdings. DBConnection centralizes the JDBC connection setup."),
    paragraph("This approach improves maintainability because UI classes remain focused on forms, tables, and feedback, while database classes remain focused on SQL statements and result handling. It also makes the project easier to test manually because database behavior is separated from page layout behavior."),
    heading(HeadingLevel.HEADING_1, "6. User Interface Design"),
    paragraph("The interface is built around five tabs: Add Expense, Insights, Investments, Transactions, and Dashboard. This layout allows the system to separate common tasks into predictable work areas. Add Expense focuses on data entry, Insights focuses on analytics, Transactions focuses on detailed records, Investments focuses on holdings, and Dashboard combines high-level summaries into one page."),
    paragraph("Consistent cards, labels, buttons, tables, and status messages are used throughout the UI. This consistency helps the application look more polished and makes it easier for a user to understand how to move between pages. Confirmation dialogs and empty states are used to make destructive actions safer and to keep the app understandable when no data is present."),
    heading(HeadingLevel.HEADING_1, "7. Database Design"),
    paragraph("The MySQL schema is intentionally compact and closely matched to the features of the app. Core tables are created in sql/fat_schema.sql, while the investments table is created automatically at runtime when needed."),
    table(["Table", "Purpose"], schemaRows, [2200, 7160]),
    paragraph("The expense and budget pages are scoped to the current month, so summary calculations are based on the current calendar month rather than an arbitrary reporting range. This keeps the design straightforward and aligned with the budget-management goals of the application."),
    heading(HeadingLevel.HEADING_1, "8. Validation and Reliability Design"),
    paragraph("Validation is handled before database operations so that bad input does not reach the SQL layer. Expense validation checks category, description, amount, and date. Budget validation checks numeric input and positive values. Investment validation checks fields such as shares, prices, ticker format, and purchase date."),
    paragraph("Reliability was also a design priority. When the database is unavailable or misconfigured, the app shows readable error messages or unavailable states instead of closing unexpectedly. This behavior reduces demo risk and makes the system feel more robust."),
    heading(HeadingLevel.HEADING_1, "9. Design Limitations and Future Improvement"),
    paragraph("The current design intentionally avoids features such as login accounts, cloud synchronization, automatic market feeds, and installer-based packaging. These features would increase the complexity of the project beyond what was needed for the assignment."),
    paragraph("Reasonable future improvements include editing existing expense records, exporting reports, background loading for slower database operations, stronger layout responsiveness, and more integration between the expense and investment views. These improvements can be added later without changing the core architecture."),
    heading(HeadingLevel.HEADING_1, "10. Conclusion"),
    paragraph("The design of FAT reflects a practical balance between structure and simplicity. The application uses recognizable software engineering ideas such as layered organization, DAO classes, and the Observer Pattern while remaining approachable for a second-year student team."),
    paragraph("As a result, the system is both technically sound and appropriate for a university group project submission."),
  ];
}

function deploymentDoc(todayText) {
  const softwareRows = [
    ["Java", "JDK 21"],
    ["Database", "MySQL Server 8.x"],
    ["Java Driver", "MySQL Connector/J"],
    ["Chart Libraries", "JFreeChart 1.0.19 and JCommon 1.0.23"],
    ["Optional Tooling", "Node.js only if the team wants to regenerate Word documents"],
  ];

  return [
    title("Deployment Document", { size: 30, pageBreakBefore: true }),
    paragraph("Financial Activity Tracker (FAT)", {
      alignment: AlignmentType.CENTER,
      bold: true,
      before: 0,
      after: 60,
    }),
    paragraph("Date: " + todayText, {
      alignment: AlignmentType.CENTER,
      before: 0,
      after: 220,
    }),
    heading(HeadingLevel.HEADING_1, "1. Purpose"),
    paragraph("This document explains how to deploy and run FAT in the intended project environment. The target deployment setup is a Windows Azure Virtual Machine with Java and MySQL installed locally on the same machine. The application is deployed manually, which fits the scope of the project and keeps the process realistic for a course demo."),
    heading(HeadingLevel.HEADING_1, "2. Deployment Environment"),
    paragraph("The recommended grading and demonstration environment is a Windows Azure VM with desktop access enabled. Because FAT is a Java Swing application, it needs a machine that can display a desktop UI. MySQL should be installed locally on the same machine so that the application can connect to the database without additional network configuration."),
    paragraph("The repository should be copied onto the VM, and the app should be compiled and launched from the project folder. The project does not use Docker, Maven, Gradle, or an installer. Instead, it runs directly through javac and java."),
    heading(HeadingLevel.HEADING_1, "3. Required Software"),
    paragraph("Before deploying the project, the following software should be installed or confirmed on the VM."),
    table(["Component", "Required version or role"], softwareRows, [2400, 6960]),
    heading(HeadingLevel.HEADING_1, "4. Database Setup"),
    paragraph("After installing MySQL Server, create a database named fat. A local user such as fat_user can be created, although root can also be used for demo purposes if the credentials match the runtime configuration."),
    paragraph("Example SQL commands:", { bold: true }),
    paragraph("CREATE DATABASE fat;", { bold: true }),
    paragraph("CREATE USER 'fat_user'@'localhost' IDENTIFIED BY 'YOUR_PASSWORD';", { bold: true }),
    paragraph("GRANT ALL PRIVILEGES ON fat.* TO 'fat_user'@'localhost';", { bold: true }),
    paragraph("FLUSH PRIVILEGES;", { bold: true }),
    paragraph("Once the database exists, run the schema file from the repository root using MySQL. This creates the users, categories, budgets, and expenses tables and inserts the default seeded data. The investments table is created automatically by the app when the Investments tab is opened."),
    heading(HeadingLevel.HEADING_1, "5. Runtime Dependencies"),
    paragraph("The lib folder must contain all required runtime JAR files. At minimum, the deployment must include mysql-connector-j-8.4.0.jar, jfreechart-1.0.19.jar, and jcommon-1.0.23.jar. If the MySQL connector is missing, the app will launch but will not be able to connect to the database successfully."),
    heading(HeadingLevel.HEADING_1, "6. Runtime Configuration"),
    paragraph("Before launching the application, the database environment variables must be set in the terminal session that will run the app. On Windows PowerShell, the following values are used:"),
    paragraph("$env:FAT_DB_URL = \"jdbc:mysql://localhost:3306/fat?serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true\"", { bold: true }),
    paragraph("$env:FAT_DB_USER = \"fat_user\"", { bold: true }),
    paragraph("$env:FAT_DB_PASSWORD = \"YOUR_PASSWORD\"", { bold: true }),
    paragraph("These values can also be supplied through Java system properties, but environment variables are the simplest and most practical method for classroom deployment."),
    heading(HeadingLevel.HEADING_1, "7. Compilation and Launch"),
    paragraph("Compile the project from the repository root with the canonical source list:"),
    paragraph("javac -cp \"lib/*\" -d out @sources.txt", { bold: true }),
    paragraph("After compilation, launch the app on Windows with:"),
    paragraph("java -cp \"out;lib/*\" app.Main", { bold: true }),
    paragraph("On macOS or Linux, the classpath separator changes to a colon, but the grading target for this project is a Windows Azure VM."),
    heading(HeadingLevel.HEADING_1, "8. Verification Checklist"),
    paragraph("After deployment, a simple smoke test should be performed to confirm the app is ready for demonstration."),
    bullet("Open the application and confirm the main window appears."),
    bullet("Go to Add Expense and verify the categories load."),
    bullet("Save a budget and confirm the save succeeds."),
    bullet("Add an expense and confirm Transactions, Dashboard, and Insights update."),
    bullet("Open Investments, add a holding, refresh the page, and confirm the table updates."),
    bullet("Delete a transaction and an investment and confirm both actions show confirmation and feedback."),
    heading(HeadingLevel.HEADING_1, "9. Troubleshooting"),
    paragraph("If the app opens but saving does not work, verify that MySQL is running, the fat database exists, the schema was loaded, and the runtime environment variables are set correctly. If a missing driver message appears, confirm that the MySQL connector JAR exists in lib. If the Investments tab appears empty, confirm the database connection and use the refresh button after opening the tab."),
    heading(HeadingLevel.HEADING_1, "10. Conclusion"),
    paragraph("The FAT deployment model is intentionally manual and straightforward. This makes it appropriate for a university project because it reduces hidden complexity and gives teaching assistants a direct way to compile, launch, and verify the application on the Azure VM."),
  ];
}

function userGuideDoc(todayText) {
  return [
    title("User Guide", { size: 30, pageBreakBefore: true }),
    paragraph("Financial Activity Tracker (FAT)", {
      alignment: AlignmentType.CENTER,
      bold: true,
      before: 0,
      after: 60,
    }),
    paragraph("Date: " + todayText, {
      alignment: AlignmentType.CENTER,
      before: 0,
      after: 220,
    }),
    heading(HeadingLevel.HEADING_1, "1. Introduction"),
    paragraph("Financial Activity Tracker (FAT) is a Java Swing desktop application that helps users manage expenses, budgets, and investment holdings. The interface is divided into tabs so that the user can move easily between entering data, reviewing transactions, and checking current-month summaries."),
    ...placeholder("Main application window after launch", "Figure 1. FAT main window and tab navigation."),
    heading(HeadingLevel.HEADING_1, "2. System Requirements"),
    paragraph("To use FAT successfully, the machine should have JDK 21 installed, MySQL Server 8.x available, and the required JAR files in the lib folder. The application uses MySQL through JDBC and uses JFreeChart for charts on the Dashboard and Insights pages."),
    heading(HeadingLevel.HEADING_1, "3. Getting Started"),
    paragraph("Before opening the application, make sure the MySQL database named fat has been created and sql/fat_schema.sql has been run. Then set the FAT_DB_URL, FAT_DB_USER, and FAT_DB_PASSWORD environment variables. Compile the project with javac and launch it with java from the repository root."),
    paragraph("If the database configuration is missing or incorrect, the application still opens but displays readable setup and availability messages. This allows the user to identify the problem without the program crashing."),
    heading(HeadingLevel.HEADING_1, "4. Interface Overview"),
    paragraph("The application is organized into five tabs: Add Expense, Insights, Investments, Transactions, and Dashboard. Each tab focuses on one main kind of work. Status messages appear directly in the interface so the user receives immediate feedback after saves, deletes, or validation failures."),
    heading(HeadingLevel.HEADING_1, "5. Feature Walkthrough by Tab"),
    paragraph("The sections below describe the purpose and normal usage of each tab."),
    heading(HeadingLevel.HEADING_2, "5.1 Add Expense"),
    paragraph("The Add Expense tab is used to record new expense entries and to set the budget for the current month. The expense form includes category, description, amount, and date fields. The budget panel allows the user to define or update a monthly spending limit."),
    bullet("Choose a category from the dropdown list."),
    bullet("Enter a description, amount, and valid date in YYYY-MM-DD format."),
    bullet("Click Add Expense to save the record."),
    bullet("Enter a budget amount and click Set Budget to update the current month target."),
    ...placeholder("Add Expense tab", "Figure 2. Add Expense tab showing the expense form and budget panel."),
    heading(HeadingLevel.HEADING_2, "5.2 Insights"),
    paragraph("The Insights tab shows current-month analytics. It includes summary cards such as top category, average daily spend, total monthly spend, average transaction size, projected month-end spending, and budget risk. A bar chart shows the top spending categories for the current month, and a tips section explains the data in simple terms."),
    ...placeholder("Insights tab", "Figure 3. Insights tab with statistics cards, chart, and financial tips."),
    heading(HeadingLevel.HEADING_2, "5.3 Investments"),
    paragraph("The Investments tab is used to track holdings manually. It includes portfolio summary cards, an add form, and a holdings table. The form collects fields such as name, ticker, type, shares, buy price, current price, and purchase date. The table also supports deletion with confirmation."),
    bullet("Enter the holding details and click Add Investment."),
    bullet("Use Refresh Portfolio if you want to manually reload the data."),
    bullet("Use the Delete action in the table to remove a holding after confirmation."),
    ...placeholder("Investments tab", "Figure 4. Investments tab with summary cards, add form, and holdings table."),
    heading(HeadingLevel.HEADING_2, "5.4 Transactions"),
    paragraph("The Transactions tab displays the expense history in a table. The page includes search, category filtering, column sorting, and delete support. It is mainly used to review past entries and remove records when necessary."),
    bullet("Search by keyword using the search field."),
    bullet("Filter by category using the dropdown."),
    bullet("Click a column header to sort the table."),
    bullet("Select a row and confirm before deleting it."),
    ...placeholder("Transactions tab", "Figure 5. Transactions tab with search, filter, and sortable table."),
    heading(HeadingLevel.HEADING_2, "5.5 Dashboard"),
    paragraph("The Dashboard tab combines high-level current-month information into one view. It includes a budget alert banner, summary cards, charts, top categories, and recent transactions. This page is especially useful in demos because it shows the results of user actions in one place."),
    ...placeholder("Dashboard tab", "Figure 6. Dashboard tab with alerts, summary cards, chart, and recent transactions."),
    heading(HeadingLevel.HEADING_1, "6. Common Tasks"),
    paragraph("Common user tasks in FAT include adding a new expense, setting the monthly budget, checking the current-month insights, finding an older transaction, and adding or deleting investment records. These actions can all be completed from the main tab interface without leaving the application."),
    bullet("Add a new expense from Add Expense."),
    bullet("Set or update the monthly budget from Add Expense."),
    bullet("Review analytics from Insights or Dashboard."),
    bullet("Search for a record from Transactions."),
    bullet("Track a holding from Investments."),
    heading(HeadingLevel.HEADING_1, "7. Validation and Error Handling"),
    paragraph("FAT validates input before saving data. Expense entries require a valid category, description, amount, and date. Budgets must be positive numbers. Investments require valid numeric values and a valid purchase date. If the user enters bad data, the app shows a clear error message in the interface."),
    paragraph("If MySQL is unavailable or not configured correctly, the app does not crash immediately. Instead, it shows setup-related feedback or unavailable states so the user can understand what needs to be fixed."),
    heading(HeadingLevel.HEADING_1, "8. Troubleshooting"),
    paragraph("If the app opens but data does not save, verify the database connection settings and confirm that MySQL is running. If categories fail to load, check the database and schema. If the app reports a missing driver, ensure mysql-connector-j-8.4.0.jar is in the lib folder. If the Investments tab appears empty, confirm the connection and use the refresh button."),
    heading(HeadingLevel.HEADING_1, "9. Conclusion"),
    paragraph("Financial Activity Tracker provides a clear desktop interface for expense tracking, budget monitoring, transaction review, analytics, and investment management. Its tab-based structure, validation, feedback messages, and chart-driven summaries make it practical for both coursework and live demonstration."),
  ];
}

function buildDoc() {
  const todayText = formatDate(new Date());

  return new Document({
    sections: [
      {
        children: [
          ...coverPage(todayText),
          ...requirementsDoc(todayText),
          ...designDoc(todayText),
          ...deploymentDoc(todayText),
          ...userGuideDoc(todayText),
        ],
      },
    ],
  });
}

async function buildAndWrite(outputPath = "FAT_Submission_Documents.docx") {
  const doc = buildDoc();
  const buffer = await Packer.toBuffer(doc);
  fs.writeFileSync(outputPath, buffer);
  return outputPath;
}

module.exports = { buildDoc, buildAndWrite };

if (require.main === module) {
  buildAndWrite().then((outputPath) => {
    console.log("Done: " + outputPath);
  });
}
