const APP_CONFIG = window.FAT_INFO ?? {};

document.addEventListener("DOMContentLoaded", () => {
    setMonthLabel();
    renderBanner();
    renderSummary();
    renderStackTable();
    renderStepsTable();
});

function setMonthLabel() {
    const label = new Date().toLocaleDateString("en-CA", { month: "long", year: "numeric" });
    document.getElementById("monthLabel").textContent = label;
}

function renderBanner() {
    const banner = document.getElementById("alertBanner");
    banner.textContent = APP_CONFIG.bannerText ?? "Desktop submission build: Java Swing + MySQL + JDBC on Azure VM.";
}

function renderSummary() {
    document.getElementById("tagline").textContent = APP_CONFIG.tagline ?? "Financial Activity Tracker";
    document.getElementById("primaryApp").textContent = APP_CONFIG.primaryApp ?? "Java Swing";
    document.getElementById("databaseType").textContent = APP_CONFIG.database ?? "MySQL";
    document.getElementById("deployTarget").textContent = APP_CONFIG.deployment ?? "Azure VM";
    document.getElementById("projectSummary").innerHTML = `
        <p>This repository's main deliverable is a desktop application built with Java Swing.</p>
        <p style="margin-top:12px">The app uses JDBC to connect to MySQL and is intended to be launched manually on an Azure VM with MySQL installed locally on the same machine.</p>
        <p style="margin-top:12px">The desktop app remains the grading target. This page is informational only.</p>
    `;
}

function renderStackTable() {
    const stackItems = APP_CONFIG.stack ?? [
        ["UI", "Java Swing"],
        ["Database", "MySQL 8.x"],
        ["Database Access", "JDBC"],
        ["Charts", "JFreeChart"],
        ["Pattern", "Observer Pattern"],
        ["Deployment", "Azure VM"],
    ];

    document.getElementById("stackTableBody").innerHTML = stackItems.map(([label, value]) => `
        <tr>
            <td><strong>${label}</strong></td>
            <td>${value}</td>
        </tr>
    `).join("");
}

function renderStepsTable() {
    const steps = APP_CONFIG.launchSteps ?? [
        "Install JDK 21 and MySQL Server on the Azure VM.",
        "Create the fat database and run sql/fat_schema.sql.",
        "Place mysql-connector-j-8.4.0.jar in lib/.",
        "Set FAT_DB_URL, FAT_DB_USER, and FAT_DB_PASSWORD.",
        "Compile with javac -cp \"lib/*\" -d out @sources.txt.",
        "Run with java -cp \"out;lib/*\" app.Main.",
    ];

    document.getElementById("stepsTableBody").innerHTML = steps.map((step, index) => `
        <tr>
            <td>${index + 1}</td>
            <td>${step}</td>
        </tr>
    `).join("");
}
