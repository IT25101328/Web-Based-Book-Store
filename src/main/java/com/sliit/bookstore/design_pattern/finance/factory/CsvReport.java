package com.sliit.bookstore.design_pattern.finance.factory;

public class CsvReport implements Report {
    @Override
    public byte[] generateReport() {
        // Generate CSV text data
        return "Fake,CSV,Data".getBytes();
    }

    @Override
    public String getContentType() {
        return "text/csv";
    }
}
