package com.digitalharbor.duckstore.order.dto;

import java.math.BigDecimal;
import java.util.List;

public record OrderResponse(
        String packageType,
        List<String> protectionTypes,
        BigDecimal total,
        List<PriceBreakdownItem> breakdown
) {
    public record PriceBreakdownItem(
            String description,
            BigDecimal amount
    ) {
    }
}