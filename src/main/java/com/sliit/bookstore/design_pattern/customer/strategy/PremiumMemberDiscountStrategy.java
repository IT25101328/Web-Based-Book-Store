package com.sliit.bookstore.design_pattern.customer.strategy;

import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class PremiumMemberDiscountStrategy implements DiscountStrategy {
    @Override
    public BigDecimal applyDiscount(BigDecimal originalPrice) {
        // 10% discount for premium members
        return originalPrice.multiply(new BigDecimal("0.90"));
    }
}
