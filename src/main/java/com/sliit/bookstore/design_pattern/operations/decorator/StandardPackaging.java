package com.sliit.bookstore.design_pattern.operations.decorator;

import java.math.BigDecimal;

public class StandardPackaging implements OrderPackaging {
    @Override
    public String getDescription() {
        return "Standard Cardboard Box";
    }

    @Override
    public BigDecimal getCost() {
        return BigDecimal.ZERO;
    }
}
