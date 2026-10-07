package com.sliit.bookstore.design_pattern.finance.factory;

import org.springframework.stereotype.Component;

@Component
public class FinanceReportFactory implements ReportFactory {
    @Override
    public Report createReport(String type) {
        if ("PDF".equalsIgnoreCase(type)) {
            return new PdfReport();
        } else if ("CSV".equalsIgnoreCase(type)) {
            return new CsvReport();
        }
        throw new IllegalArgumentException("Unknown report type: " + type);
    }
}
