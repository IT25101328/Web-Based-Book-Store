package com.sliit.bookstore.design_pattern.finance.factory;

public class PdfReport implements Report {
    @Override
    public byte[] generateReport() {
        // Generate PDF binary data
        return "Fake PDF Data".getBytes();
    }

    @Override
    public String getContentType() {
        return "application/pdf";
    }
}
