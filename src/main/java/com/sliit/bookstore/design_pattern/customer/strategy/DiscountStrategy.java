package com.sliit.bookstore.design_pattern.customer.strategy;

import java.math.BigDecimal;

public interface DiscountStrategy {
    BigDecimal applyDiscount(BigDecimal originalPrice);
}
