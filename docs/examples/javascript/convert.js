/**
 * XToPDF API - JavaScript (Node.js) Usage Examples
 *
 * Requirements:
 *   Node.js 18+ (native fetch API)
 *
 * Usage:
 *   node convert.js
 */

const fs = require("fs");
const path = require("path");

const BASE_URL = "http://localhost:8080";

/**
 * Convert a single file to PDF.
 */
async function basicConvert(filePath, outputName = "output.pdf") {
  const file = fs.readFileSync(filePath);
  const formData = new FormData();
  formData.append("file", new Blob([file]), path.basename(filePath));

  const response = await fetch(
    `${BASE_URL}/api/convert?outputFileName=${encodeURIComponent(outputName)}`,
    {
      method: "POST",
      body: formData,
    }
  );

  if (!response.ok) {
    const error = await response.json();
    throw new Error(`Conversion failed (${response.status}): ${error.message}`);
  }

  const pdfBuffer = Buffer.from(await response.arrayBuffer());
  console.log(`Conversion successful: ${pdfBuffer.length} bytes`);
  return pdfBuffer;
}

/**
 * Convert multiple files in a single batch request.
 */
async function batchConvert(filePaths) {
  const formData = new FormData();

  for (const filePath of filePaths) {
    const file = fs.readFileSync(filePath);
    formData.append("files", new Blob([file]), path.basename(filePath));
  }

  const response = await fetch(`${BASE_URL}/api/convert/batch`, {
    method: "POST",
    body: formData,
  });

  if (!response.ok) {
    const error = await response.json();
    throw new Error(
      `Batch conversion failed (${response.status}): ${error.message}`
    );
  }

  const resultBuffer = Buffer.from(await response.arrayBuffer());
  console.log(`Batch conversion successful: ${resultBuffer.length} bytes`);
  return resultBuffer;
}

/**
 * Submit an async conversion job and poll for completion.
 */
async function asyncConvert(filePath, webhookUrl = null) {
  const file = fs.readFileSync(filePath);
  const formData = new FormData();
  formData.append("file", new Blob([file]), path.basename(filePath));

  let url = `${BASE_URL}/api/convert/async`;
  if (webhookUrl) {
    url += `?webhookUrl=${encodeURIComponent(webhookUrl)}`;
  }

  const submitResponse = await fetch(url, {
    method: "POST",
    body: formData,
  });

  if (submitResponse.status !== 202) {
    const error = await submitResponse.json();
    throw new Error(
      `Job submission failed (${submitResponse.status}): ${error.message}`
    );
  }

  const job = await submitResponse.json();
  const jobId = job.jobId;
  console.log(`Job submitted: ${jobId}`);

  // Poll for completion
  while (true) {
    const statusResponse = await fetch(
      `${BASE_URL}/api/convert/async/${jobId}`
    );
    const status = await statusResponse.json();
    console.log(`  Status: ${status.status}`);

    if (status.status === "COMPLETED") {
      const resultResponse = await fetch(
        `${BASE_URL}/api/convert/async/${jobId}/result`
      );
      const pdfBuffer = Buffer.from(await resultResponse.arrayBuffer());
      console.log(`Result downloaded: ${pdfBuffer.length} bytes`);
      return { job: status, content: pdfBuffer };
    }

    if (status.status === "FAILED") {
      throw new Error(`Job failed: ${status.errorMessage || "Unknown error"}`);
    }

    await new Promise((resolve) => setTimeout(resolve, 2000));
  }
}

/**
 * Add a watermark to an existing PDF.
 */
async function addWatermark(pdfPath, text, layer = "FOREGROUND") {
  const file = fs.readFileSync(pdfPath);
  const formData = new FormData();
  formData.append("file", new Blob([file]), path.basename(pdfPath));

  const params = new URLSearchParams({ text, layer });
  const response = await fetch(
    `${BASE_URL}/api/pdf/watermark?${params.toString()}`,
    {
      method: "POST",
      body: formData,
    }
  );

  if (!response.ok) {
    const error = await response.json();
    throw new Error(`Watermark failed (${response.status}): ${error.message}`);
  }

  const pdfBuffer = Buffer.from(await response.arrayBuffer());
  console.log(`Watermark added: ${pdfBuffer.length} bytes`);
  return pdfBuffer;
}

/**
 * Add page numbers to an existing PDF.
 */
async function addPageNumbers(
  pdfPath,
  position = "BOTTOM",
  alignment = "CENTER",
  style = "ARABIC"
) {
  const file = fs.readFileSync(pdfPath);
  const formData = new FormData();
  formData.append("file", new Blob([file]), path.basename(pdfPath));

  const params = new URLSearchParams({ position, alignment, style });
  const response = await fetch(
    `${BASE_URL}/api/pdf/page-numbers?${params.toString()}`,
    {
      method: "POST",
      body: formData,
    }
  );

  if (!response.ok) {
    const error = await response.json();
    throw new Error(
      `Page numbers failed (${response.status}): ${error.message}`
    );
  }

  const pdfBuffer = Buffer.from(await response.arrayBuffer());
  console.log(`Page numbers added: ${pdfBuffer.length} bytes`);
  return pdfBuffer;
}

/**
 * Merge two PDF files.
 */
async function mergePdfs(pdfPath, overlayPath, position = "back") {
  const file1 = fs.readFileSync(pdfPath);
  const file2 = fs.readFileSync(overlayPath);
  const formData = new FormData();
  formData.append("file", new Blob([file1]), path.basename(pdfPath));
  formData.append("overlayFile", new Blob([file2]), path.basename(overlayPath));

  const response = await fetch(
    `${BASE_URL}/api/pdf/merge?position=${position}`,
    {
      method: "POST",
      body: formData,
    }
  );

  if (!response.ok) {
    const error = await response.json();
    throw new Error(`Merge failed (${response.status}): ${error.message}`);
  }

  const pdfBuffer = Buffer.from(await response.arrayBuffer());
  console.log(`Merge successful: ${pdfBuffer.length} bytes`);
  return pdfBuffer;
}

// --- Main ---
async function main() {
  console.log("=== XToPDF JavaScript Examples ===\n");

  console.log("1. Basic conversion:");
  console.log('   const pdf = await basicConvert("document.docx");');
  console.log('   fs.writeFileSync("output.pdf", pdf);\n');

  console.log("2. Batch conversion:");
  console.log(
    '   const zip = await batchConvert(["file1.docx", "file2.xlsx"]);\n'
  );

  console.log("3. Async conversion:");
  console.log('   const result = await asyncConvert("large-file.xlsx");\n');

  console.log("4. Async with webhook:");
  console.log(
    '   const result = await asyncConvert("large.xlsx", "https://myapp.com/hook");\n'
  );

  console.log("5. Add watermark:");
  console.log(
    '   const pdf = await addWatermark("doc.pdf", "CONFIDENTIAL");\n'
  );

  console.log("6. Add page numbers:");
  console.log("   const pdf = await addPageNumbers(\"doc.pdf\");\n");

  console.log("7. Merge PDFs:");
  console.log('   const pdf = await mergePdfs("main.pdf", "appendix.pdf");\n');
}

main().catch(console.error);

module.exports = {
  basicConvert,
  batchConvert,
  asyncConvert,
  addWatermark,
  addPageNumbers,
  mergePdfs,
};
