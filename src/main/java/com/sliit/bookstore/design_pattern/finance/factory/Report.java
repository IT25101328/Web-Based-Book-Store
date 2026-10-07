package com.sliit.bookstore.design_pattern.finance.factory;

public interface Report {
    byte[] generateReport();
    String getContentType();
}
