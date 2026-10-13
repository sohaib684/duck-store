
package com.digitalharbor.duckstore.duck.dto;

import com.digitalharbor.duckstore.duck.DuckColor;
import com.digitalharbor.duckstore.duck.DuckSize;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateDuckRequest(

        @NotNull(message = "Color is required")
        DuckColor color,

        @NotNull(message = "Size is required")
        DuckSize size,

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.01", message = "Price must be greater than zero")
        @Digits(integer = 10, fraction = 2)
        BigDecimal price,

        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        Integer quantity

) {
}
