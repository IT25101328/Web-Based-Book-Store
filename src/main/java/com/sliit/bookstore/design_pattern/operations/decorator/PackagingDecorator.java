package com.sliit.bookstore.design_pattern.operations.decorator;

import java.math.BigDecimal;

public abstract class PackagingDecorator implements OrderPackaging {
    protected OrderPackaging decoratedPackaging;

    public PackagingDecorator(OrderPackaging decoratedPackaging) {
        this.decoratedPackaging = decoratedPackaging;
    }

    @Override
    public String getDescription() {
        return decoratedPackaging.getDescription();
    }

    @Override
    public BigDecimal getCost() {
        return decoratedPackaging.getCost();
    }
}
