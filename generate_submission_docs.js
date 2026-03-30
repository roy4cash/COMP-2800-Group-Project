const path = require("path");
const { buildAndWrite } = require("./docs/submission_docs_builder");

buildAndWrite(path.join(__dirname, "FAT_Submission_Documents.docx"))
  .then((outputPath) => {
    console.log("Done: " + outputPath);
  })
  .catch((error) => {
    console.error("Failed to generate FAT_Submission_Documents.docx");
    console.error(error);
    process.exit(1);
  });
