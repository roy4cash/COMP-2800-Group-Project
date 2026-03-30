window.FAT_INFO = {
    tagline: "Financial Activity Tracker",
    primaryApp: "Java Swing",
    database: "MySQL",
    deployment: "Azure VM",
    bannerText: "Submission-ready desktop build: Java Swing + MySQL + JDBC.",
    stack: [
        ["UI", "Java Swing"],
        ["Database", "MySQL 8.x"],
        ["Database Access", "JDBC"],
        ["Charts", "JFreeChart"],
        ["Pattern", "Observer Pattern"],
        ["Deployment", "Azure VM"]
    ],
    launchSteps: [
        "Install JDK 21 and MySQL Server on the Azure VM.",
        "Create the fat database and run sql/fat_schema.sql.",
        "Place mysql-connector-j-8.4.0.jar in lib/.",
        "Set FAT_DB_URL, FAT_DB_USER, and FAT_DB_PASSWORD.",
        "Compile with javac -cp \"lib/*\" -d out @sources.txt.",
        "Run with java -cp \"out;lib/*\" app.Main."
    ]
};
