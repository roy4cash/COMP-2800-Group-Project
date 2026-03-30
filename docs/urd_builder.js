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

function heading(level, text) {
  return new Paragraph({
    heading: level,
    spacing: { before: 280, after: 120 },
    children: [new TextRun({ text, bold: true })],
  });
}

function paragraph(text, options = {}) {
  return new Paragraph({
    alignment: options.alignment || AlignmentType.LEFT,
    spacing: { before: options.before || 80, after: options.after || 100 },
    children: [new TextRun({ text, bold: !!options.bold, size: options.size || 22 })],
  });
}

function bullet(text) {
  return new Paragraph({
    spacing: { before: 30, after: 30 },
    children: [new TextRun({ text: "- " + text, size: 22 })],
  });
}

function cell(text, options = {}) {
  return new TableCell({
    borders,
    width: options.width ? { size: options.width, type: WidthType.DXA } : undefined,
    margins: { top: 80, bottom: 80, left: 120, right: 120 },
    children: [
      new Paragraph({
        children: [new TextRun({ text: String(text), bold: !!options.bold, size: 20 })],
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

function buildDoc() {
  const today = new Date();
  const todayText = formatDate(today);

  const overviewRows = [
    ["Primary deliverable", "Java Swing desktop application"],
    ["Database", "MySQL via JDBC"],
    ["Charts", "JFreeChart 1.0.19"],
    ["Secondary artifact", "Static project information page in web/"],
    ["Documentation workflow", "Node + docx generator"],
    ["Deployment target", "Azure VM with local MySQL installation"],
    ["Current user model", "Single-user default session (user_id = 1)"],
  ];

  const dependencyRows = [
    ["Runtime", "JDK 21 recommended"],
    ["Database", "MySQL Server 8.x"],
    ["Java libraries", "lib/mysql-connector-j-8.4.0.jar or compatible Connector/J, lib/jfreechart-1.0.19.jar, lib/jcommon-1.0.23.jar"],
    ["Node tooling", "Only required to regenerate this document"],
    ["Database config", "FAT_DB_URL, FAT_DB_USER, FAT_DB_PASSWORD or matching Java properties"],
  ];

  const functionalRequirements = [
    ["FR-01", "Single-user launch", "The application launches into a default user session backed by user_id = 1.", "Implemented"],
    ["FR-02", "Add expense", "Users can add an expense with category, description, amount, and date.", "Implemented"],
    ["FR-03", "Delete expense", "Users can delete a selected expense after confirmation.", "Implemented"],
    ["FR-04", "Monthly budget", "Users can create or update the current month's budget.", "Implemented"],
    ["FR-05", "Dashboard summary", "The dashboard shows monthly budget, total spent, remaining amount, and budget progress.", "Implemented"],
    ["FR-06", "Budget alerts", "The app shows neutral, warning, and over-budget states based on monthly spending.", "Implemented"],
    ["FR-07", "Transactions table", "The app shows expense history with delete confirmation and empty-state handling.", "Implemented"],
    ["FR-08", "Insights tab", "The app shows top category, average daily spend, monthly totals, bar chart, and tips.", "Implemented"],
    ["FR-09", "Investments tab", "Users can add, refresh, and delete investment holdings.", "Implemented"],
    ["FR-10", "Validation and feedback", "The app validates user input and shows inline success and error feedback.", "Implemented"],
    ["FR-11", "Configurable database connection", "Database access is configured at runtime through environment variables or Java properties.", "Implemented"],
    ["FR-12", "Automatic investment table creation", "The investments table is created automatically if it does not already exist.", "Implemented"],
  ];

  const nonFunctionalRequirements = [
    ["NFR-01", "Reliability", "The UI should show actionable error messages instead of crashing on missing database settings or failed queries.", "Implemented"],
    ["NFR-02", "Usability", "Common actions should provide immediate success or error feedback and clear empty states.", "Implemented"],
    ["NFR-03", "Maintainability", "The codebase is organized into app, model, db, observer, ui, and util packages.", "Implemented"],
    ["NFR-04", "Manual deployability", "The desktop app can be deployed manually to an Azure VM with Java and MySQL installed locally.", "Implemented"],
    ["NFR-05", "Documentation alignment", "README and the docs/ directory are aligned with the current implementation.", "Implemented"],
  ];

  const architectureRows = [
    ["Presentation", "Swing UI in src/ui, coordinated by MainFrame"],
    ["Business logic", "ExpenseManager mediates expenses, budgets, and observer updates"],
    ["Data access", "DAO classes in src/db execute SQL through JDBC"],
    ["Investments path", "InvestmentPanel talks directly to InvestmentDAO"],
    ["Database", "MySQL; users, categories, budgets, and expenses come from fat_schema.sql, while investments are created at runtime when needed"],
    ["Optional web path", "web/ is a static informational page and is separate from desktop runtime"],
  ];

  const schemaRows = [
    ["users", "id, username"],
    ["categories", "id, name"],
    ["budgets", "id, user_id, month, year, amount"],
    ["expenses", "id, user_id, category_id, description, amount, expense_date, created_at"],
    ["investments", "id, user_id, name, ticker, type, shares, buy_price, current_price, purchase_date, notes, created_at"],
  ];

  const deploymentSteps = [
    "Install JDK 21 on the Azure VM or target machine.",
    "Install MySQL Server 8.x on the same machine and create the fat database.",
    "Run sql/fat_schema.sql against the MySQL database.",
    "Place MySQL Connector/J in lib/.",
    "Set FAT_DB_URL, FAT_DB_USER, and FAT_DB_PASSWORD.",
    "Compile from the repository root with javac -cp \"lib/*\" -d out @sources.txt.",
    "Launch with java -cp \"out;lib/*\" app.Main on Windows or java -cp \"out:lib/*\" app.Main on macOS/Linux.",
    "Open the Investments tab once if you need the investments table to be created automatically.",
  ];

  const userGuideRows = [
    ["Add Expense", "Add expenses and set the current month's budget with inline validation and feedback."],
    ["Insights", "Review monthly spending stats, top categories, and generated tips."],
    ["Investments", "Track holdings separately from the expense dashboard flow."],
    ["Transactions", "Review expense history and delete selected transactions with confirmation."],
    ["Dashboard", "Review budget alerts, summary cards, and the monthly spending pie chart."],
  ];

  const limitationRows = [
    ["Authentication", "No login flow is implemented. The app uses a single default user."],
    ["Infrastructure automation", "No automated Azure, Docker, or installer workflow is included."],
    ["Budget scope", "Budget summaries and charts are scoped to the current month."],
    ["Investment integration", "Investments do not contribute to budget calculations or dashboard alerts."],
  ];

  return new Document({
    sections: [
      {
        children: [
          paragraph("Financial Activity Tracker (FAT)", {
            alignment: AlignmentType.CENTER,
            bold: true,
            size: 34,
            before: 400,
            after: 120,
          }),
          paragraph("Submission-ready User Requirements and Design Summary", {
            alignment: AlignmentType.CENTER,
            size: 24,
            before: 0,
            after: 220,
          }),
          table(
            ["Field", "Value"],
            [
              ["Document", "FAT_User_Requirements.docx"],
              ["Version", "Submission-aligned revision"],
              ["Date", todayText],
              ["Primary implementation", "Java Swing desktop app"],
              ["Repository scope", "Desktop app, static project page, and documentation generator"],
            ],
            [2400, 6960]
          ),

          heading(HeadingLevel.HEADING_1, "1. Project Overview"),
          paragraph("FAT is a desktop application for tracking expenses, budgets, and investments. This document summarizes the implementation that is actually present in the repository and is intended to stay aligned with the source tree used for submission."),
          table(["Area", "Current implementation"], overviewRows, [2400, 6960]),

          heading(HeadingLevel.HEADING_1, "2. Dependencies and Setup"),
          paragraph("The desktop application is compiled directly with javac using JARs in lib/. Node.js is only needed to regenerate this Word document."),
          table(["Dependency", "Details"], dependencyRows, [2400, 6960]),

          heading(HeadingLevel.HEADING_1, "3. Functional Requirements"),
          table(["ID", "Requirement", "Description", "Status"], functionalRequirements, [900, 2200, 4760, 1500]),

          heading(HeadingLevel.HEADING_1, "4. Non-Functional Requirements"),
          table(["ID", "Area", "Description", "Status"], nonFunctionalRequirements, [900, 1600, 5360, 1500]),

          heading(HeadingLevel.HEADING_1, "5. Architecture Summary"),
          paragraph("The repository contains a direct-to-database desktop application. There is no backend API layer. Expense and budget updates use an observer flow centered on ExpenseManager. Investments are implemented as a parallel path that talks directly to InvestmentDAO."),
          table(["Layer", "Current implementation"], architectureRows, [2400, 6960]),

          heading(HeadingLevel.HEADING_1, "6. Database Design"),
          paragraph("The checked-in MySQL schema defines users, categories, budgets, and expenses. The investments table is created automatically by the desktop app when needed."),
          table(["Table", "Columns"], schemaRows, [2200, 7160]),

          heading(HeadingLevel.HEADING_1, "7. Deployment Steps"),
          paragraph("The current deployment model is manual. The repository does not include infrastructure automation or a packaged installer."),
          ...deploymentSteps.map((step, index) => paragraph((index + 1) + ". " + step)),

          heading(HeadingLevel.HEADING_1, "8. User Guide Summary"),
          table(["Tab", "Current behavior"], userGuideRows, [1800, 7560]),

          heading(HeadingLevel.HEADING_1, "9. Known Constraints"),
          table(["Area", "Current limitation"], limitationRows, [2200, 7160]),

          heading(HeadingLevel.HEADING_1, "10. Submission Notes"),
          bullet("README.md is the marker-facing starting point."),
          bullet("docs/DEPENDENCIES.md, docs/DEPLOYMENT.md, docs/USER_GUIDE.md, and docs/DESIGN.md provide detailed aligned documentation."),
          bullet("sources.txt is the canonical Java source list for compilation."),
          bullet("The desktop app is the primary deliverable. The web page is informational only."),
        ],
      },
    ],
  });
}

async function buildAndWrite(outputPath = "FAT_User_Requirements.docx") {
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
