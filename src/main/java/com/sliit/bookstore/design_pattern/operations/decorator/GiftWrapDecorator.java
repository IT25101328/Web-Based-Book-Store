package com.sliit.bookstore.design_pattern.operations.decorator;

import java.math.BigDecimal;

public class GiftWrapDecorator extends PackagingDecorator {
    public GiftWrapDecorator(OrderPackaging decoratedPackaging) {
        super(decoratedPackaging);
    }

    @Override
    public String getDescription() {
        return super.getDescription() + ", Premium Gift Wrap";
    }

    @Override
    public BigDecimal getCost() {
        return super.getCost().add(new BigDecimal("250.00"));
    }
}
