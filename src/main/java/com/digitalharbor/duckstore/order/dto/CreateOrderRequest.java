package com.digitalharbor.duckstore.order.dto;

import com.digitalharbor.duckstore.duck.DuckColor;
import com.digitalharbor.duckstore.duck.DuckSize;
import com.digitalharbor.duckstore.order.ShippingMode;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateOrderRequest(
        @NotNull DuckColor color,
        @NotNull DuckSize size,
        @NotNull @Min(1) Integer quantity,
        @NotBlank String country,
        @NotNull ShippingMode shippingMode
) {
}