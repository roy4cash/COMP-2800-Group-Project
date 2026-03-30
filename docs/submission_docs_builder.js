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
