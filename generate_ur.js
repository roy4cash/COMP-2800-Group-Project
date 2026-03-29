const {
  Document, Packer, Paragraph, TextRun, Table, TableRow, TableCell,
  AlignmentType, HeadingLevel, BorderStyle, WidthType, ShadingType,
  VerticalAlign, PageNumber, Header, Footer, LevelFormat, PageBreak
} = require('docx');
const fs = require('fs');

const BLUE   = "1F3864";
const LBLUE  = "D6E4F0";
const MBLUE  = "2E75B6";
const WHITE  = "FFFFFF";
const GRAY   = "F2F2F2";
const DGRAY  = "404040";

const border = { style: BorderStyle.SINGLE, size: 1, color: "CCCCCC" };
const borders = { top: border, bottom: border, left: border, right: border };
const noBorder = { style: BorderStyle.NONE, size: 0, color: "FFFFFF" };
const noBorders = { top: noBorder, bottom: noBorder, left: noBorder, right: noBorder };

function h(level, text) {
  return new Paragraph({
    heading: level,
    spacing: { before: 280, after: 120 },
    children: [new TextRun({ text, bold: true })]
  });
}

function p(text, options = {}) {
  return new Paragraph({
    spacing: { before: 60, after: 100 },
    children: [new TextRun({ text, size: 22, color: DGRAY, ...options })]
  });
}

function bullet(text, bold = false) {
  return new Paragraph({
    numbering: { reference: "bullets", level: 0 },
    spacing: { before: 40, after: 40 },
    children: [new TextRun({ text, size: 22, color: DGRAY, bold })]
  });
}

function subbullet(text) {
  return new Paragraph({
    numbering: { reference: "subbullets", level: 0 },
    spacing: { before: 30, after: 30 },
    children: [new TextRun({ text, size: 20, color: DGRAY })]
  });
}

function spacer() {
  return new Paragraph({ spacing: { before: 60, after: 60 }, children: [new TextRun("")] });
}

function sectionRule() {
  return new Paragraph({
    border: { bottom: { style: BorderStyle.SINGLE, size: 6, color: MBLUE, space: 1 } },
    spacing: { before: 0, after: 160 },
    children: [new TextRun("")]
  });
}

function hdrCell(text, width) {
  return new TableCell({
    borders,
    width: { size: width, type: WidthType.DXA },
    shading: { fill: BLUE, type: ShadingType.CLEAR },
    margins: { top: 80, bottom: 80, left: 140, right: 140 },
    children: [new Paragraph({ children: [new TextRun({ text, bold: true, color: WHITE, size: 20 })] })]
  });
}

function cell(text, width, shade = false) {
  return new TableCell({
    borders,
    width: { size: width, type: WidthType.DXA },
    shading: { fill: shade ? GRAY : WHITE, type: ShadingType.CLEAR },
    margins: { top: 80, bottom: 80, left: 140, right: 140 },
    children: [new Paragraph({ children: [new TextRun({ text, size: 20, color: DGRAY })] })]
  });
}

function reqTable(rows) {
  const cols = [900, 3500, 2500, 1700, 760];
  return new Table({
    width: { size: 9360, type: WidthType.DXA },
    columnWidths: cols,
    rows: [
      new TableRow({
        tableHeader: true,
        children: [
          hdrCell("ID", cols[0]),
          hdrCell("Requirement", cols[1]),
          hdrCell("Description", cols[2]),
          hdrCell("Priority", cols[3]),
          hdrCell("Status", cols[4]),
        ]
      }),
      ...rows.map(([id, req, desc, pri, status], i) =>
        new TableRow({
          children: [
            cell(id, cols[0], i % 2 === 0),
            cell(req, cols[1], i % 2 === 0),
            cell(desc, cols[2], i % 2 === 0),
            cell(pri, cols[3], i % 2 === 0),
            cell(status, cols[4], i % 2 === 0),
          ]
        })
      )
    ]
  });
}

function ucTable(rows) {
  const cols = [1200, 3000, 5160];
  return new Table({
    width: { size: 9360, type: WidthType.DXA },
    columnWidths: cols,
    rows: [
      new TableRow({
        tableHeader: true,
        children: [
          hdrCell("Field", cols[0]),
          hdrCell("", cols[1]),
          hdrCell("Detail", cols[2]),
        ]
      }),
      ...rows.map(([field, , detail], i) =>
        new TableRow({
          children: [
            cell(field, cols[0], true),
            cell("", cols[1], i % 2 === 0),
            cell(detail, cols[2], i % 2 === 0),
          ]
        })
      )
    ]
  });
}

const doc = new Document({
  numbering: {
    config: [
      {
        reference: "bullets",
        levels: [{
          level: 0, format: LevelFormat.BULLET, text: "\u2022",
          alignment: AlignmentType.LEFT,
          style: { paragraph: { indent: { left: 600, hanging: 300 } } }
        }]
      },
      {
        reference: "subbullets",
        levels: [{
          level: 0, format: LevelFormat.BULLET, text: "\u25E6",
          alignment: AlignmentType.LEFT,
          style: { paragraph: { indent: { left: 1000, hanging: 300 } } }
        }]
      },
      {
        reference: "numbered",
        levels: [{
          level: 0, format: LevelFormat.DECIMAL, text: "%1.",
          alignment: AlignmentType.LEFT,
          style: { paragraph: { indent: { left: 600, hanging: 300 } } }
        }]
      }
    ]
  },
  styles: {
    default: {
      document: { run: { font: "Calibri", size: 22 } }
    },
    paragraphStyles: [
      {
        id: "Heading1", name: "Heading 1", basedOn: "Normal", next: "Normal", quickFormat: true,
        run: { size: 30, bold: true, font: "Calibri", color: BLUE },
        paragraph: { spacing: { before: 320, after: 160 }, outlineLevel: 0 }
      },
      {
        id: "Heading2", name: "Heading 2", basedOn: "Normal", next: "Normal", quickFormat: true,
        run: { size: 26, bold: true, font: "Calibri", color: MBLUE },
        paragraph: { spacing: { before: 220, after: 100 }, outlineLevel: 1 }
      },
      {
        id: "Heading3", name: "Heading 3", basedOn: "Normal", next: "Normal", quickFormat: true,
        run: { size: 23, bold: true, font: "Calibri", color: DGRAY },
        paragraph: { spacing: { before: 180, after: 80 }, outlineLevel: 2 }
      }
    ]
  },
  sections: [{
    properties: {
      page: {
        size: { width: 12240, height: 15840 },
        margin: { top: 1080, right: 1080, bottom: 1080, left: 1080 }
      }
    },
    headers: {
      default: new Header({
        children: [
          new Paragraph({
            border: { bottom: { style: BorderStyle.SINGLE, size: 4, color: MBLUE, space: 1 } },
            tabStops: [{ type: "right", position: 9360 }],
            children: [
              new TextRun({ text: "Financial Activity Tracker (FAT)", bold: true, color: MBLUE, size: 18 }),
              new TextRun({ text: "\tUser Requirements Document", color: DGRAY, size: 18 })
            ]
          })
        ]
      })
    },
    footers: {
      default: new Footer({
        children: [
          new Paragraph({
            border: { top: { style: BorderStyle.SINGLE, size: 4, color: MBLUE, space: 1 } },
            tabStops: [{ type: "right", position: 9360 }],
            alignment: AlignmentType.LEFT,
            children: [
              new TextRun({ text: "COMP-2800 | University of Windsor | 2026", size: 18, color: DGRAY }),
              new TextRun({ text: "\tPage ", size: 18, color: DGRAY }),
              new TextRun({ children: [PageNumber.CURRENT], size: 18, color: DGRAY }),
              new TextRun({ text: " of ", size: 18, color: DGRAY }),
              new TextRun({ children: [PageNumber.TOTAL_PAGES], size: 18, color: DGRAY }),
            ]
          })
        ]
      })
    },
    children: [
      // ── COVER ────────────────────────────────────────────────────────────────
      new Paragraph({ spacing: { before: 1200, after: 0 }, children: [] }),

      new Paragraph({
        alignment: AlignmentType.CENTER,
        spacing: { before: 0, after: 100 },
        children: [new TextRun({ text: "COMP-2800 — Software Development", size: 22, color: DGRAY })]
      }),
      new Paragraph({
        alignment: AlignmentType.CENTER,
        spacing: { before: 0, after: 200 },
        children: [new TextRun({ text: "University of Windsor  |  Winter 2026", size: 22, color: DGRAY })]
      }),

      new Paragraph({
        alignment: AlignmentType.CENTER,
        spacing: { before: 0, after: 60 },
        border: {
          bottom: { style: BorderStyle.SINGLE, size: 12, color: MBLUE, space: 1 }
        },
        children: [new TextRun({ text: "Financial Activity Tracker", bold: true, size: 56, color: BLUE, font: "Calibri" })]
      }),
      new Paragraph({
        alignment: AlignmentType.CENTER,
        spacing: { before: 80, after: 400 },
        children: [new TextRun({ text: "User Requirements Document  (URD)", size: 30, color: MBLUE, bold: true })]
      }),

      new Table({
        width: { size: 6000, type: WidthType.DXA },
        columnWidths: [2400, 3600],
        rows: [
          new TableRow({ children: [
            cell("Project", 2400, true),
            cell("Financial Activity Tracker (FAT)", 3600, true)
          ]}),
          new TableRow({ children: [
            cell("Document Version", 2400),
            cell("1.0", 3600)
          ]}),
          new TableRow({ children: [
            cell("Date", 2400, true),
            cell("March 28, 2026", 3600, true)
          ]}),
          new TableRow({ children: [
            cell("Course", 2400),
            cell("COMP-2800 — Software Development", 3600)
          ]}),
          new TableRow({ children: [
            cell("Status", 2400, true),
            cell("Final", 3600, true)
          ]}),
        ]
      }),

      spacer(),

      new Paragraph({
        spacing: { before: 200, after: 40 },
        children: [new TextRun({ text: "Group Members", bold: true, size: 22, color: BLUE })]
      }),
      new Table({
        width: { size: 9360, type: WidthType.DXA },
        columnWidths: [3120, 2340, 3900],
        rows: [
          new TableRow({ children: [hdrCell("Name", 3120), hdrCell("Student ID", 2340), hdrCell("Email", 3900)] }),
          new TableRow({ children: [cell("Abdul Batha", 3120, false), cell("110178126", 2340, false), cell("bathaa@uwindsor.ca", 3900, false)] }),
          new TableRow({ children: [cell("Mohammad Affan Shahid", 3120, true), cell("110134589", 2340, true), cell("shahid62@uwindsor.ca", 3900, true)] }),
          new TableRow({ children: [cell("Ayushmaan Roy", 3120, false), cell("110168184", 2340, false), cell("roy4c@uwindsor.ca", 3900, false)] }),
          new TableRow({ children: [cell("Aadi Bhatia", 3120, true), cell("110171730", 2340, true), cell("bhatia29@uwindsor.ca", 3900, true)] }),
        ]
      }),

      new Paragraph({ children: [new PageBreak()] }),

      // ── 1. INTRODUCTION ──────────────────────────────────────────────────────
      h(HeadingLevel.HEADING_1, "1. Introduction"),
      sectionRule(),

      h(HeadingLevel.HEADING_2, "1.1 Purpose"),
      p("This User Requirements Document (URD) defines the functional and non-functional requirements for the Financial Activity Tracker (FAT) — a desktop application built in Java. FAT enables users to log expenses, set monthly budgets, view spending summaries, and receive alerts when budgets are close to being exceeded."),
      p("This document serves as the agreed-upon contract between the development team and stakeholders. All implemented features must trace back to a requirement listed here."),

      spacer(),
      h(HeadingLevel.HEADING_2, "1.2 Scope"),
      p("FAT is a standalone Java Swing desktop application with a Supabase (PostgreSQL) cloud database back-end. It is targeted at students and young professionals who want a lightweight tool to monitor and manage personal finances without the complexity of full-featured banking applications."),
      p("The application is deployed on an Azure Virtual Machine and is accessible by running the compiled JAR. It does not process real payment data or integrate with financial institutions."),

      spacer(),
      h(HeadingLevel.HEADING_2, "1.3 Definitions and Acronyms"),
      new Table({
        width: { size: 9360, type: WidthType.DXA },
        columnWidths: [2000, 7360],
        rows: [
          new TableRow({ children: [hdrCell("Term", 2000), hdrCell("Definition", 7360)] }),
          ...[
            ["FAT", "Financial Activity Tracker — the name of this application."],
            ["URD", "User Requirements Document — this document."],
            ["JDBC", "Java Database Connectivity — the API used to connect Java to the Supabase PostgreSQL database."],
            ["DAO", "Data Access Object — a design pattern used to abstract database operations."],
            ["Observer Pattern", "A software design pattern used to notify UI components of data changes automatically."],
            ["JFreeChart", "An open-source Java charting library used to render the spending pie chart."],
            ["Supabase", "A cloud-hosted PostgreSQL database platform used as the persistent data store."],
            ["Azure VM", "Microsoft Azure Virtual Machine — the cloud server on which the application is deployed."],
            ["FR", "Functional Requirement."],
            ["NFR", "Non-Functional Requirement."],
          ].map(([term, def], i) => new TableRow({ children: [cell(term, 2000, i % 2 === 0), cell(def, 7360, i % 2 === 0)] }))
        ]
      }),

      new Paragraph({ children: [new PageBreak()] }),

      // ── 2. OVERALL DESCRIPTION ───────────────────────────────────────────────
      h(HeadingLevel.HEADING_1, "2. Overall System Description"),
      sectionRule(),

      h(HeadingLevel.HEADING_2, "2.1 System Context"),
      p("FAT operates as a desktop client that communicates with a cloud-hosted PostgreSQL database via JDBC. The user interacts with a Java Swing graphical interface. All data — expenses, categories, and budgets — is persisted in Supabase and is accessible from any machine that runs the JAR with network access."),

      spacer(),
      h(HeadingLevel.HEADING_2, "2.2 User Classes"),
      new Table({
        width: { size: 9360, type: WidthType.DXA },
        columnWidths: [2200, 3580, 3580],
        rows: [
          new TableRow({ children: [hdrCell("User Class", 2200), hdrCell("Description", 3580), hdrCell("Interaction Level", 3580)] }),
          ...[
            ["General User", "Students or young professionals tracking personal expenses and budgets.", "Full access to all features: add/delete expenses, set budgets, view charts."],
            ["TA / Evaluator", "Teaching assistant or instructor reviewing the running application.", "Read and interact with all features on the deployed Azure VM."],
            ["Developer", "Team members maintaining and extending the codebase.", "Access to source code, database schema, and VM configuration."],
          ].map(([uc, desc, inter], i) => new TableRow({ children: [cell(uc, 2200, i % 2 === 0), cell(desc, 3580, i % 2 === 0), cell(inter, 3580, i % 2 === 0)] }))
        ]
      }),

      spacer(),
      h(HeadingLevel.HEADING_2, "2.3 Assumptions and Dependencies"),
      bullet("The application requires a live internet connection to communicate with the Supabase cloud database."),
      bullet("Java 21 (JDK) must be installed on the machine running the application."),
      bullet("Required JAR libraries (PostgreSQL JDBC driver 42.7.10, JFreeChart 1.0.19, JCommon 1.0.23) must be present in the lib/ directory."),
      bullet("The Supabase project (wmrkmjdzgbtojpkmmzkr) remains active and accessible throughout evaluation."),
      bullet("The Azure VM maintains an active and publicly reachable environment during the evaluation period."),
      bullet("All team members have access to the shared Supabase instance."),

      new Paragraph({ children: [new PageBreak()] }),

      // ── 3. FUNCTIONAL REQUIREMENTS ───────────────────────────────────────────
      h(HeadingLevel.HEADING_1, "3. Functional Requirements"),
      sectionRule(),
      p("The following table lists all functional requirements for FAT. Priority is rated: High (must have), Medium (should have), Low (nice to have)."),
      spacer(),

      reqTable([
        ["FR-01", "User Login / Session", "The application launches with a default user session (user_id = 1). All data is scoped to this user.", "High", "Done"],
        ["FR-02", "Add Expense", "Users can add a new expense by entering a description, amount, category, and date. The record is persisted to the database immediately.", "High", "Done"],
        ["FR-03", "Delete Expense", "Users can delete any existing expense from the expense table. The record is removed from the database.", "High", "Done"],
        ["FR-04", "Categorise Expense", "Each expense must be assigned to one of the predefined categories (Food, Transport, Entertainment, Utilities, Health, Education, Shopping, Other).", "High", "Done"],
        ["FR-05", "View Expense History", "All recorded expenses are displayed in a scrollable table showing date, category, description, and amount, sorted by date descending.", "High", "Done"],
        ["FR-06", "Set Monthly Budget", "Users can set or update a monthly budget for the current calendar month. The value is saved to the database using upsert logic.", "High", "Done"],
        ["FR-07", "Budget Summary Display", "The dashboard displays the monthly budget, total amount spent, and remaining balance at all times.", "High", "Done"],
        ["FR-08", "Budget Alert — 80% Warning", "The system displays a yellow warning alert when spending reaches 80% or more of the monthly budget.", "High", "Done"],
        ["FR-09", "Budget Alert — Over Budget", "The system displays a red critical alert and message when spending exceeds the monthly budget.", "High", "Done"],
        ["FR-10", "Spending Pie Chart", "A doughnut-style pie chart visualises spending broken down by category for the current month, rendered using JFreeChart.", "Medium", "Done"],
        ["FR-11", "Input Validation", "The application validates all form inputs: amount must be positive, description must not be blank, date must be selected. Appropriate error messages are shown.", "High", "Done"],
        ["FR-12", "Observer Pattern Notification", "The application uses the Observer design pattern so that all UI panels (dashboard, table, chart, alert) refresh automatically when expense or budget data changes.", "High", "Done"],
        ["FR-13", "Database Persistence", "All expense and budget data is stored persistently in the Supabase PostgreSQL cloud database and is retained across application restarts.", "High", "Done"],
        ["FR-14", "Category Management", "Predefined categories are seeded into the database and loaded at startup. Users cannot add or remove categories in this version.", "Low", "Done"],
        ["FR-15", "Monthly Scoping", "Expense summaries, budgets, and charts are scoped to the current calendar month only.", "Medium", "Done"],
      ]),

      new Paragraph({ children: [new PageBreak()] }),

      // ── 4. NON-FUNCTIONAL REQUIREMENTS ──────────────────────────────────────
      h(HeadingLevel.HEADING_1, "4. Non-Functional Requirements"),
      sectionRule(),

      reqTable([
        ["NFR-01", "Performance", "The application must load and display all expenses and summaries within 3 seconds of launch on a standard internet connection.", "High", "Done"],
        ["NFR-02", "Reliability", "Database operations (insert, delete, fetch) must succeed consistently. If a connection fails, the application must display a meaningful error message rather than crashing.", "High", "Done"],
        ["NFR-03", "Usability", "The interface must be navigable by a first-time user without training. Labels, buttons, and form fields must be clearly identified.", "High", "Done"],
        ["NFR-04", "Maintainability", "The codebase must follow a layered architecture (model, db, observer, ui, util) with in-code comments on all classes and key methods.", "High", "Done"],
        ["NFR-05", "Portability", "The application must run on any machine with Java 21 installed and access to the lib/ folder, without additional installation steps.", "Medium", "Done"],
        ["NFR-06", "Security", "Database credentials must not be hardcoded in any publicly visible file. The Supabase anon key used in any web context is restricted by Row-Level Security (RLS).", "Medium", "Done"],
        ["NFR-07", "Scalability", "The database schema and DAO layer must be designed so that additional users, categories, or features can be added without restructuring the existing schema.", "Low", "Done"],
        ["NFR-08", "Deployment", "The application must be fully deployed and runnable on an Azure Virtual Machine. A TA must be able to launch and interact with it without developer assistance.", "High", "In Progress"],
      ]),

      new Paragraph({ children: [new PageBreak()] }),

      // ── 5. USE CASES ─────────────────────────────────────────────────────────
      h(HeadingLevel.HEADING_1, "5. Use Cases"),
      sectionRule(),

      h(HeadingLevel.HEADING_2, "UC-01: Add a New Expense"),
      new Table({
        width: { size: 9360, type: WidthType.DXA },
        columnWidths: [2400, 6960],
        rows: [
          new TableRow({ children: [hdrCell("Field", 2400), hdrCell("Detail", 6960)] }),
          ...[
            ["Use Case ID", "UC-01"],
            ["Use Case Name", "Add a New Expense"],
            ["Actor", "General User"],
            ["Precondition", "Application is running and connected to the database."],
            ["Main Flow", "1. User navigates to the Add Expense panel.\n2. User selects a category from the dropdown.\n3. User enters a description.\n4. User enters an amount (positive number).\n5. User selects a date.\n6. User clicks Add Expense.\n7. System validates all fields.\n8. System inserts the expense into the database.\n9. All UI panels refresh automatically via the Observer pattern."],
            ["Alternate Flow", "If any field is invalid (empty description, non-positive amount, no date selected), the system displays an inline error message and does not submit."],
            ["Postcondition", "The new expense appears in the expense table and is reflected in the summary and chart."],
          ].map(([field, detail], i) => new TableRow({ children: [cell(field, 2400, true), cell(detail, 6960, i % 2 === 0)] }))
        ]
      }),

      spacer(),
      h(HeadingLevel.HEADING_2, "UC-02: Set a Monthly Budget"),
      new Table({
        width: { size: 9360, type: WidthType.DXA },
        columnWidths: [2400, 6960],
        rows: [
          new TableRow({ children: [hdrCell("Field", 2400), hdrCell("Detail", 6960)] }),
          ...[
            ["Use Case ID", "UC-02"],
            ["Use Case Name", "Set a Monthly Budget"],
            ["Actor", "General User"],
            ["Precondition", "Application is running. A budget for the current month may or may not already exist."],
            ["Main Flow", "1. User navigates to the Budget panel.\n2. User enters a budget amount.\n3. User clicks Set Budget.\n4. System upserts the budget record for the current month and year.\n5. Dashboard updates to reflect the new budget."],
            ["Alternate Flow", "If the amount is zero or negative, the system displays an error and does not save."],
            ["Postcondition", "The new budget is displayed on the dashboard and alert thresholds are recalculated."],
          ].map(([field, detail], i) => new TableRow({ children: [cell(field, 2400, true), cell(detail, 6960, i % 2 === 0)] }))
        ]
      }),

      spacer(),
      h(HeadingLevel.HEADING_2, "UC-03: Receive a Budget Alert"),
      new Table({
        width: { size: 9360, type: WidthType.DXA },
        columnWidths: [2400, 6960],
        rows: [
          new TableRow({ children: [hdrCell("Field", 2400), hdrCell("Detail", 6960)] }),
          ...[
            ["Use Case ID", "UC-03"],
            ["Use Case Name", "Receive a Budget Alert"],
            ["Actor", "System (triggered automatically)"],
            ["Precondition", "A monthly budget has been set and at least one expense has been recorded."],
            ["Main Flow", "1. User adds an expense that brings total spending to >= 80% of the budget.\n2. System recalculates the budget ratio.\n3. System displays a yellow warning alert: 'Warning: You have used X% of your monthly budget.'\n4. If spending exceeds 100%, the alert turns red: 'Over budget!'"],
            ["Alternate Flow", "If no budget is set, no alert is shown."],
            ["Postcondition", "The alert panel reflects the current spending status in real time."],
          ].map(([field, detail], i) => new TableRow({ children: [cell(field, 2400, true), cell(detail, 6960, i % 2 === 0)] }))
        ]
      }),

      spacer(),
      h(HeadingLevel.HEADING_2, "UC-04: Delete an Expense"),
      new Table({
        width: { size: 9360, type: WidthType.DXA },
        columnWidths: [2400, 6960],
        rows: [
          new TableRow({ children: [hdrCell("Field", 2400), hdrCell("Detail", 6960)] }),
          ...[
            ["Use Case ID", "UC-04"],
            ["Use Case Name", "Delete an Expense"],
            ["Actor", "General User"],
            ["Precondition", "At least one expense exists in the expense table."],
            ["Main Flow", "1. User locates the expense in the expense table.\n2. User clicks the Delete button on the target row.\n3. System removes the record from the database.\n4. Expense table and summary panels refresh automatically."],
            ["Alternate Flow", "N/A — the delete button is only shown for existing records."],
            ["Postcondition", "The expense is removed and all totals and charts are recalculated."],
          ].map(([field, detail], i) => new TableRow({ children: [cell(field, 2400, true), cell(detail, 6960, i % 2 === 0)] }))
        ]
      }),

      new Paragraph({ children: [new PageBreak()] }),

      // ── 6. SYSTEM ARCHITECTURE ───────────────────────────────────────────────
      h(HeadingLevel.HEADING_1, "6. System Architecture"),
      sectionRule(),

      h(HeadingLevel.HEADING_2, "6.1 Technology Stack"),
      new Table({
        width: { size: 9360, type: WidthType.DXA },
        columnWidths: [2400, 3480, 3480],
        rows: [
          new TableRow({ children: [hdrCell("Layer", 2400), hdrCell("Technology", 3480), hdrCell("Purpose", 3480)] }),
          ...[
            ["Presentation", "Java Swing (javax.swing)", "Graphical user interface — panels, tables, forms, charts"],
            ["Visualization", "JFreeChart 1.0.19", "Pie/doughnut chart for spending by category"],
            ["Business Logic", "Java 21 (Observer Pattern)", "ExpenseManager notifies all registered UI observers on data change"],
            ["Data Access", "DAO Pattern (JDBC)", "ExpenseDAO, BudgetDAO, CategoryDAO abstract all SQL operations"],
            ["Database", "Supabase (PostgreSQL)", "Cloud-hosted persistent data store for expenses, budgets, and categories"],
            ["Deployment", "Azure Virtual Machine", "Hosts the compiled Java application, accessible for live evaluation"],
          ].map(([layer, tech, purpose], i) => new TableRow({ children: [cell(layer, 2400, true), cell(tech, 3480, i % 2 === 0), cell(purpose, 3480, i % 2 === 0)] }))
        ]
      }),

      spacer(),
      h(HeadingLevel.HEADING_2, "6.2 Package Structure"),
      bullet("src/app/ — Entry point (Main.java). Initialises the application and launches MainFrame."),
      bullet("src/model/ — POJOs: Expense, Budget, Category, User."),
      bullet("src/db/ — Database layer: DBConnection, ExpenseDAO, BudgetDAO, CategoryDAO."),
      bullet("src/observer/ — Observer pattern: Subject interface, Observer interface, ExpenseManager."),
      bullet("src/ui/ — All Swing panels: MainFrame, DashboardPanel, AddExpensePanel, ExpenseTablePanel, SummaryPanel, AlertPanel, ChartPanel."),
      bullet("src/util/ — Utility classes: ValidationUtils, DateUtils."),
      bullet("sql/ — fat_schema.sql: PostgreSQL DDL for all tables and seed data."),
      bullet("lib/ — External JAR dependencies (postgresql, jfreechart, jcommon)."),

      spacer(),
      h(HeadingLevel.HEADING_2, "6.3 Database Schema"),
      new Table({
        width: { size: 9360, type: WidthType.DXA },
        columnWidths: [2000, 1800, 1500, 4060],
        rows: [
          new TableRow({ children: [hdrCell("Table", 2000), hdrCell("Column", 1800), hdrCell("Type", 1500), hdrCell("Description", 4060)] }),
          ...[
            ["users", "id", "SERIAL PK", "Auto-incremented user identifier"],
            ["users", "username", "VARCHAR(50)", "Unique username for the account"],
            ["users", "email", "VARCHAR(100)", "User email address"],
            ["categories", "id", "SERIAL PK", "Auto-incremented category identifier"],
            ["categories", "name", "VARCHAR(50)", "Category name (e.g., Food, Transport)"],
            ["expenses", "id", "SERIAL PK", "Auto-incremented expense identifier"],
            ["expenses", "user_id", "INT FK", "References users(id)"],
            ["expenses", "category_id", "INT FK", "References categories(id)"],
            ["expenses", "description", "VARCHAR(255)", "Text description of the expense"],
            ["expenses", "amount", "DECIMAL(10,2)", "Expense amount in dollars"],
            ["expenses", "expense_date", "DATE", "Date the expense was incurred"],
            ["budgets", "id", "SERIAL PK", "Auto-incremented budget identifier"],
            ["budgets", "user_id", "INT FK", "References users(id)"],
            ["budgets", "month", "INT", "Calendar month (1-12)"],
            ["budgets", "year", "INT", "Calendar year"],
            ["budgets", "amount", "DECIMAL(10,2)", "Monthly budget limit in dollars"],
          ].map(([table, col, type, desc], i) => new TableRow({ children: [cell(table, 2000, i % 2 === 0), cell(col, 1800, i % 2 === 0), cell(type, 1500, i % 2 === 0), cell(desc, 4060, i % 2 === 0)] }))
        ]
      }),

      new Paragraph({ children: [new PageBreak()] }),

      // ── 7. TRACEABILITY MATRIX ───────────────────────────────────────────────
      h(HeadingLevel.HEADING_1, "7. Requirements Traceability Matrix"),
      sectionRule(),
      p("The following matrix maps each functional requirement to the Java source files that implement it and the use cases that exercise it."),
      spacer(),

      new Table({
        width: { size: 9360, type: WidthType.DXA },
        columnWidths: [900, 2200, 3760, 2500],
        rows: [
          new TableRow({ children: [hdrCell("FR ID", 900), hdrCell("Requirement", 2200), hdrCell("Implementing Class(es)", 3760), hdrCell("Use Case", 2500)] }),
          ...[
            ["FR-01", "User Login", "Main.java, DBConnection.java", "All"],
            ["FR-02", "Add Expense", "AddExpensePanel.java, ExpenseDAO.java, ExpenseManager.java", "UC-01"],
            ["FR-03", "Delete Expense", "ExpenseTablePanel.java, ExpenseDAO.java", "UC-04"],
            ["FR-04", "Categorise Expense", "CategoryDAO.java, AddExpensePanel.java", "UC-01"],
            ["FR-05", "View Expense History", "ExpenseTablePanel.java, ExpenseDAO.java", "UC-01, UC-04"],
            ["FR-06", "Set Monthly Budget", "SummaryPanel.java, BudgetDAO.java", "UC-02"],
            ["FR-07", "Budget Summary", "DashboardPanel.java, BudgetDAO.java, ExpenseDAO.java", "UC-02, UC-03"],
            ["FR-08", "80% Warning Alert", "AlertPanel.java, ExpenseManager.java", "UC-03"],
            ["FR-09", "Over Budget Alert", "AlertPanel.java, ExpenseManager.java", "UC-03"],
            ["FR-10", "Pie Chart", "ChartPanel.java, ExpenseDAO.java, JFreeChart", "All"],
            ["FR-11", "Input Validation", "ValidationUtils.java, AddExpensePanel.java", "UC-01"],
            ["FR-12", "Observer Pattern", "Subject.java, Observer.java, ExpenseManager.java", "All"],
            ["FR-13", "DB Persistence", "DBConnection.java, all DAO classes", "All"],
            ["FR-14", "Category Management", "CategoryDAO.java, fat_schema.sql", "UC-01"],
            ["FR-15", "Monthly Scoping", "ExpenseDAO.java, BudgetDAO.java", "All"],
          ].map(([id, req, cls, uc], i) => new TableRow({ children: [cell(id, 900, i % 2 === 0), cell(req, 2200, i % 2 === 0), cell(cls, 3760, i % 2 === 0), cell(uc, 2500, i % 2 === 0)] }))
        ]
      }),

      new Paragraph({ children: [new PageBreak()] }),

      // ── 8. ACCEPTANCE CRITERIA ───────────────────────────────────────────────
      h(HeadingLevel.HEADING_1, "8. Acceptance Criteria"),
      sectionRule(),
      p("The following criteria must all be satisfied for the project to be considered complete and ready for evaluation:"),
      spacer(),

      new Table({
        width: { size: 9360, type: WidthType.DXA },
        columnWidths: [900, 5660, 2800],
        rows: [
          new TableRow({ children: [hdrCell("#", 900), hdrCell("Acceptance Criterion", 5660), hdrCell("Verified By", 2800)] }),
          ...[
            ["AC-01", "The application launches without errors on the Azure VM with the command: java -cp 'out;lib/*' app.Main", "TA runs the command and app opens"],
            ["AC-02", "A new expense can be added and immediately appears in the expense table.", "TA adds a test expense and observes it in the table"],
            ["AC-03", "An expense can be deleted and immediately disappears from the table.", "TA deletes an expense and confirms removal"],
            ["AC-04", "The monthly budget can be set and the dashboard updates immediately.", "TA sets a budget and confirms summary updates"],
            ["AC-05", "A yellow warning alert appears when spending reaches 80% of the budget.", "TA adds expenses to reach 80% threshold and observes alert"],
            ["AC-06", "A red over-budget alert appears when spending exceeds the budget.", "TA adds expenses beyond the budget and observes alert"],
            ["AC-07", "The spending pie chart displays correctly segmented by category.", "TA observes chart after adding expenses in multiple categories"],
            ["AC-08", "All data persists after closing and reopening the application.", "TA closes app, reopens, and confirms data is still present"],
            ["AC-09", "The source code is accessible via a public GitHub repository link.", "TA clicks the repository link and can browse all source files"],
            ["AC-10", "All Java classes contain in-code comments describing their purpose.", "TA browses source code and finds comments on all classes and key methods"],
          ].map(([num, crit, verif], i) => new TableRow({ children: [cell(num, 900, i % 2 === 0), cell(crit, 5660, i % 2 === 0), cell(verif, 2800, i % 2 === 0)] }))
        ]
      }),

      new Paragraph({ children: [new PageBreak()] }),

      // ── 9. REVISION HISTORY ──────────────────────────────────────────────────
      h(HeadingLevel.HEADING_1, "9. Revision History"),
      sectionRule(),

      new Table({
        width: { size: 9360, type: WidthType.DXA },
        columnWidths: [1200, 1800, 4560, 1800],
        rows: [
          new TableRow({ children: [hdrCell("Version", 1200), hdrCell("Date", 1800), hdrCell("Changes", 4560), hdrCell("Author", 1800)] }),
          ...[
            ["0.1", "March 20, 2026", "Initial draft — scope and functional requirements", "Aadi Bhatia"],
            ["0.2", "March 24, 2026", "Added non-functional requirements, use cases, and traceability matrix", "Ayushmaan Roy"],
            ["1.0", "March 28, 2026", "Final version — added acceptance criteria, architecture section, and revised all requirements to match implemented system", "All Members"],
          ].map(([ver, date, changes, author], i) => new TableRow({ children: [cell(ver, 1200, i % 2 === 0), cell(date, 1800, i % 2 === 0), cell(changes, 4560, i % 2 === 0), cell(author, 1800, i % 2 === 0)] }))
        ]
      }),

      spacer(), spacer(),
      new Paragraph({
        alignment: AlignmentType.CENTER,
        border: { top: { style: BorderStyle.SINGLE, size: 6, color: MBLUE, space: 1 } },
        spacing: { before: 400, after: 0 },
        children: [new TextRun({ text: "End of User Requirements Document", bold: true, color: MBLUE, size: 20 })]
      }),
    ]
  }]
});

Packer.toBuffer(doc).then(buffer => {
  fs.writeFileSync("FAT_User_Requirements.docx", buffer);
  console.log("Done: FAT_User_Requirements.docx");
});
