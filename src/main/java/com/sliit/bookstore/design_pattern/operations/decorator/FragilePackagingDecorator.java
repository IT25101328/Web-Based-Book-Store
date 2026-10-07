package com.sliit.bookstore.design_pattern.operations.decorator;

import java.math.BigDecimal;

public class FragilePackagingDecorator extends PackagingDecorator {
    public FragilePackagingDecorator(OrderPackaging decoratedPackaging) {
        super(decoratedPackaging);
    }

    @Override
    public String getDescription() {
        return super.getDescription() + ", Bubble Wrap & Fragile Stickers";
    }

    @Override
    public BigDecimal getCost() {
        return super.getCost().add(new BigDecimal("100.00"));
    }
}
