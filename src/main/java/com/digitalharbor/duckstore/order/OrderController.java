
package com.digitalharbor.duckstore.order;

import com.digitalharbor.duckstore.order.dto.CreateOrderRequest;
import com.digitalharbor.duckstore.order.dto.OrderResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderPricingService orderPricingService;

    public OrderController(OrderPricingService orderPricingService) {
        this.orderPricingService = orderPricingService;
    }

    @PostMapping
    public OrderResponse calculateOrder(
            @Valid @RequestBody CreateOrderRequest request
    ) {
        return orderPricingService.calculatePrice(request);
    }
}
