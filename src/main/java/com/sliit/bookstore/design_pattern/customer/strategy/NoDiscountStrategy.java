package com.sliit.bookstore.design_pattern.customer.strategy;

import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class NoDiscountStrategy implements DiscountStrategy {
    @Override
    public BigDecimal applyDiscount(BigDecimal originalPrice) {
        return originalPrice;
    }
}
