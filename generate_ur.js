const path = require("path");
const { buildAndWrite } = require("./docs/urd_builder");

buildAndWrite(path.join(__dirname, "FAT_User_Requirements.docx"))
  .then((outputPath) => {
    console.log("Done: " + outputPath);
  })
  .catch((error) => {
    console.error("Failed to generate FAT_User_Requirements.docx");
    console.error(error);
    process.exit(1);
  });
