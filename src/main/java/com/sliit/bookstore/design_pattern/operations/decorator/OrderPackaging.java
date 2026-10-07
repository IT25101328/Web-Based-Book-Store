package com.sliit.bookstore.design_pattern.operations.decorator;

import java.math.BigDecimal;

public interface OrderPackaging {
    String getDescription();
    BigDecimal getCost();
}
