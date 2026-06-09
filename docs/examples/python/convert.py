#!/usr/bin/env python3
"""
XToPDF API - Python Usage Examples

Requirements:
    pip install requests

Usage:
    python convert.py
"""

import requests
import time
import sys

BASE_URL = "http://localhost:8080"


def basic_convert(file_path: str, output_name: str = "output.pdf") -> bytes:
    """Convert a single file to PDF."""
    with open(file_path, "rb") as f:
        response = requests.post(
            f"{BASE_URL}/api/convert",
            files={"file": (file_path.split("/")[-1], f)},
            params={"outputFileName": output_name},
        )

    if response.status_code == 200:
        print(f"Conversion successful: {len(response.content)} bytes")
        return response.content
    else:
        print(f"Conversion failed: {response.status_code}")
        print(response.json())
        sys.exit(1)


def batch_convert(file_paths: list[str]) -> bytes:
    """Convert multiple files to PDF in a single batch request."""
    files = []
    for path in file_paths:
        with open(path, "rb") as f:
            files.append(("files", (path.split("/")[-1], f.read())))

    response = requests.post(
        f"{BASE_URL}/api/convert/batch",
        files=files,
    )

    if response.status_code == 200:
        print(f"Batch conversion successful: {len(response.content)} bytes")
        return response.content
    else:
        print(f"Batch conversion failed: {response.status_code}")
        print(response.json())
        sys.exit(1)


def async_convert(file_path: str, webhook_url: str = None) -> dict:
    """Submit an async conversion job and poll for completion."""
    with open(file_path, "rb") as f:
        params = {}
        if webhook_url:
            params["webhookUrl"] = webhook_url

        response = requests.post(
            f"{BASE_URL}/api/convert/async",
            files={"file": (file_path.split("/")[-1], f)},
            params=params,
        )

    if response.status_code != 202:
        print(f"Job submission failed: {response.status_code}")
        print(response.json())
        sys.exit(1)

    job = response.json()
    job_id = job["jobId"]
    print(f"Job submitted: {job_id}")

    # Poll for completion
    while True:
        status_response = requests.get(f"{BASE_URL}/api/convert/async/{job_id}")
        status = status_response.json()

        print(f"  Status: {status['status']}")

        if status["status"] == "COMPLETED":
            # Download the result
            result_response = requests.get(
                f"{BASE_URL}/api/convert/async/{job_id}/result"
            )
            print(f"Result downloaded: {len(result_response.content)} bytes")
            return {"job": status, "content": result_response.content}

        elif status["status"] == "FAILED":
            print(f"Job failed: {status.get('errorMessage', 'Unknown error')}")
            sys.exit(1)

        time.sleep(2)


def add_watermark(pdf_path: str, text: str, layer: str = "FOREGROUND") -> bytes:
    """Add a watermark to an existing PDF."""
    with open(pdf_path, "rb") as f:
        response = requests.post(
            f"{BASE_URL}/api/pdf/watermark",
            files={"file": (pdf_path.split("/")[-1], f)},
            params={"text": text, "layer": layer},
        )

    if response.status_code == 200:
        print(f"Watermark added: {len(response.content)} bytes")
        return response.content
    else:
        print(f"Watermark failed: {response.status_code}")
        print(response.json())
        sys.exit(1)


def add_page_numbers(
    pdf_path: str,
    position: str = "BOTTOM",
    alignment: str = "CENTER",
    style: str = "ARABIC",
) -> bytes:
    """Add page numbers to an existing PDF."""
    with open(pdf_path, "rb") as f:
        response = requests.post(
            f"{BASE_URL}/api/pdf/page-numbers",
            files={"file": (pdf_path.split("/")[-1], f)},
            params={
                "position": position,
                "alignment": alignment,
                "style": style,
            },
        )

    if response.status_code == 200:
        print(f"Page numbers added: {len(response.content)} bytes")
        return response.content
    else:
        print(f"Page numbers failed: {response.status_code}")
        print(response.json())
        sys.exit(1)


def merge_pdfs(pdf_path: str, overlay_path: str, position: str = "back") -> bytes:
    """Merge two PDF files."""
    with open(pdf_path, "rb") as f1, open(overlay_path, "rb") as f2:
        response = requests.post(
            f"{BASE_URL}/api/pdf/merge",
            files={
                "file": (pdf_path.split("/")[-1], f1),
                "overlayFile": (overlay_path.split("/")[-1], f2),
            },
            params={"position": position},
        )

    if response.status_code == 200:
        print(f"Merge successful: {len(response.content)} bytes")
        return response.content
    else:
        print(f"Merge failed: {response.status_code}")
        print(response.json())
        sys.exit(1)


if __name__ == "__main__":
    print("=== XToPDF Python Examples ===\n")

    # Example: Basic conversion
    print("1. Basic conversion:")
    print('   pdf = basic_convert("document.docx")')
    print()

    # Example: Batch conversion
    print("2. Batch conversion:")
    print('   pdf = batch_convert(["file1.docx", "file2.xlsx", "file3.png"])')
    print()

    # Example: Async conversion
    print("3. Async conversion with polling:")
    print('   result = async_convert("large-file.xlsx")')
    print()

    # Example: Async with webhook
    print("4. Async conversion with webhook:")
    print('   result = async_convert("large-file.xlsx", webhook_url="https://myapp.com/webhook")')
    print()

    # Example: PDF operations
    print("5. Add watermark:")
    print('   pdf = add_watermark("document.pdf", "CONFIDENTIAL")')
    print()

    print("6. Add page numbers:")
    print('   pdf = add_page_numbers("document.pdf", position="BOTTOM", alignment="CENTER")')
    print()

    print("7. Merge PDFs:")
    print('   pdf = merge_pdfs("main.pdf", "appendix.pdf", position="back")')
