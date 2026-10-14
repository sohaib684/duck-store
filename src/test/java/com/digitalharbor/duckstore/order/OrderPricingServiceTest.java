
package com.digitalharbor.duckstore.order;

import com.digitalharbor.duckstore.duck.Duck;
import com.digitalharbor.duckstore.duck.DuckColor;
import com.digitalharbor.duckstore.duck.DuckRepository;
import com.digitalharbor.duckstore.duck.DuckSize;
import com.digitalharbor.duckstore.order.dto.CreateOrderRequest;
import com.digitalharbor.duckstore.order.dto.OrderResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OrderPricingServiceTest {

    private DuckRepository duckRepository;
    private PackagingService packagingService;
    private OrderPricingService pricingService;

    @BeforeEach
    void setUp() {
        duckRepository = mock(DuckRepository.class);
        packagingService = new PackagingService();
        pricingService = new OrderPricingService(
                duckRepository, packagingService
        );

        Duck duck = new Duck();
        duck.setColor(DuckColor.RED);
        duck.setSize(DuckSize.SMALL);
        duck.setPrice(new BigDecimal("10.00"));
        duck.setQuantity(100);

        when(duckRepository
                .findByColorAndSizeAndDeletedFalseOrderByPriceAsc(
                        DuckColor.RED, DuckSize.SMALL))
                .thenReturn(List.of(duck));
    }

    @Test
    void calculatesNormalLandOrderPrice() {
        // Base: $100, plastic surcharge: $10,
        // India surcharge: $19, land shipping: $100
        CreateOrderRequest request = new CreateOrderRequest(
                DuckColor.RED, DuckSize.SMALL, 10,
                "India", ShippingMode.LAND
        );

        OrderResponse response = pricingService.calculatePrice(request);

        assertEquals(new BigDecimal("229.00"), response.total());
    }

    @Test
    void appliesBulkDiscountAboveOneHundredUnits() {
        Duck duck = new Duck();
        duck.setColor(DuckColor.RED);
        duck.setSize(DuckSize.SMALL);
        duck.setPrice(new BigDecimal("10.00"));

        when(duckRepository
                .findByColorAndSizeAndDeletedFalseOrderByPriceAsc(
                        DuckColor.RED, DuckSize.SMALL))
                .thenReturn(List.of(duck));

        CreateOrderRequest request = new CreateOrderRequest(
                DuckColor.RED, DuckSize.SMALL, 101,
                "India", ShippingMode.LAND
        );

        OrderResponse response = pricingService.calculatePrice(request);

        assertTrue(response.breakdown().stream().anyMatch(item ->
                item.description().equals("Bulk discount (20%)")
                        && item.amount().compareTo(
                        new BigDecimal("-202.00")) == 0
        ));
    }

    @Test
    void appliesAirShippingDiscountAboveOneThousandUnits() {
        Duck duck = new Duck();
        duck.setColor(DuckColor.RED);
        duck.setSize(DuckSize.SMALL);
        duck.setPrice(new BigDecimal("10.00"));

        when(duckRepository
                .findByColorAndSizeAndDeletedFalseOrderByPriceAsc(
                        DuckColor.RED, DuckSize.SMALL))
                .thenReturn(List.of(duck));

        CreateOrderRequest request = new CreateOrderRequest(
                DuckColor.RED, DuckSize.SMALL, 1001,
                "USA", ShippingMode.AIR
        );

        OrderResponse response = pricingService.calculatePrice(request);

        assertTrue(response.breakdown().stream().anyMatch(item ->
                item.description().equals("Air shipping discount (15%)")
                        && item.amount().compareTo(
                        new BigDecimal("-4504.50")) == 0
        ));
    }

    @Test
    void returnsNotFoundWhenNoMatchingDuckExists() {
        when(duckRepository
                .findByColorAndSizeAndDeletedFalseOrderByPriceAsc(
                        DuckColor.RED, DuckSize.SMALL))
                .thenReturn(List.of());

        CreateOrderRequest request = new CreateOrderRequest(
                DuckColor.RED, DuckSize.SMALL, 1,
                "India", ShippingMode.LAND
        );

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> pricingService.calculatePrice(request)
        );

        assertEquals(404, exception.getStatusCode().value());
    }
}
