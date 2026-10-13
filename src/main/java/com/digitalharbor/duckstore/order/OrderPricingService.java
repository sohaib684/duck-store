
package com.digitalharbor.duckstore.order;

import com.digitalharbor.duckstore.duck.Duck;
import com.digitalharbor.duckstore.duck.DuckRepository;
import com.digitalharbor.duckstore.order.dto.CreateOrderRequest;
import com.digitalharbor.duckstore.order.dto.OrderResponse;
import com.digitalharbor.duckstore.order.dto.OrderResponse.PriceBreakdownItem;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class OrderPricingService {

    private static final BigDecimal TWENTY_PERCENT =
            new BigDecimal("0.20");
    private static final BigDecimal FIFTEEN_PERCENT =
            new BigDecimal("0.15");

    private final DuckRepository duckRepository;
    private final PackagingService packagingService;

    public OrderPricingService(
            DuckRepository duckRepository,
            PackagingService packagingService
    ) {
        this.duckRepository = duckRepository;
        this.packagingService = packagingService;
    }

    public OrderResponse calculatePrice(CreateOrderRequest request) {

        Duck duck = duckRepository
                .findByColorAndSizeAndDeletedFalseOrderByPriceAsc(
                        request.color(),
                        request.size()
                )
                .stream()
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No active duck found for the requested color and size"
                ));

        BigDecimal baseCost = money(
                duck.getPrice().multiply(
                        BigDecimal.valueOf(request.quantity())
                )
        );

        PackagingResult packaging = packagingService.determinePackaging(
                request.size(),
                request.shippingMode()
        );

        List<PriceBreakdownItem> breakdown = new ArrayList<>();
        breakdown.add(new PriceBreakdownItem(
                "Base cost (" + request.quantity()
                        + " × $" + duck.getPrice() + ")",
                baseCost
        ));

        if (request.quantity() > 100) {
            addAdjustment(
                    breakdown,
                    "Bulk discount (20%)",
                    percentage(baseCost, TWENTY_PERCENT).negate()
            );
        }

        switch (packaging.packageType()) {
            case WOOD -> addAdjustment(
                    breakdown,
                    "Wood packaging surcharge (5%)",
                    percentage(baseCost, new BigDecimal("0.05"))
            );

            case PLASTIC -> addAdjustment(
                    breakdown,
                    "Plastic packaging surcharge (10%)",
                    percentage(baseCost, new BigDecimal("0.10"))
            );

            case CARDBOARD -> addAdjustment(
                    breakdown,
                    "Cardboard packaging discount (1%)",
                    percentage(baseCost, new BigDecimal("0.01")).negate()
            );
        }

        addCountrySurcharge(breakdown, baseCost, request.country());

        switch (request.shippingMode()) {
            case SEA -> addAdjustment(
                    breakdown,
                    "Sea shipping fee",
                    new BigDecimal("400.00")
            );

            case LAND -> addAdjustment(
                    breakdown,
                    "Land shipping fee ($10 per unit)",
                    BigDecimal.TEN.multiply(
                            BigDecimal.valueOf(request.quantity())
                    )
            );

            case AIR -> {
                BigDecimal airCharge = new BigDecimal("30.00")
                        .multiply(BigDecimal.valueOf(request.quantity()));

                addAdjustment(
                        breakdown,
                        "Air shipping fee ($30 per unit)",
                        airCharge
                );

                if (request.quantity() > 1000) {
                    addAdjustment(
                            breakdown,
                            "Air shipping discount (15%)",
                            percentage(airCharge, FIFTEEN_PERCENT).negate()
                    );
                }
            }
        }

        BigDecimal total = breakdown.stream()
                .map(PriceBreakdownItem::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new OrderResponse(
                packaging.packageType().getDisplayName(),
                packaging.protectionTypes(),
                money(total),
                List.copyOf(breakdown)
        );
    }

    private void addCountrySurcharge(
            List<PriceBreakdownItem> breakdown,
            BigDecimal baseCost,
            String country
    ) {
        String normalized = country.trim().toUpperCase(Locale.ROOT);

        switch (normalized) {
            case "USA", "UNITED STATES", "UNITED STATES OF AMERICA" ->
                    addAdjustment(
                            breakdown,
                            "USA destination surcharge (18%)",
                            percentage(baseCost, new BigDecimal("0.18"))
                    );

            case "BOLIVIA" -> addAdjustment(
                    breakdown,
                    "Bolivia destination surcharge (13%)",
                    percentage(baseCost, new BigDecimal("0.13"))
            );

            case "INDIA" -> addAdjustment(
                    breakdown,
                    "India destination surcharge (19%)",
                    percentage(baseCost, new BigDecimal("0.19"))
            );

            default -> addAdjustment(
                    breakdown,
                    "Other destination surcharge (15%)",
                    percentage(baseCost, FIFTEEN_PERCENT)
            );
        }
    }

    private void addAdjustment(
            List<PriceBreakdownItem> breakdown,
            String description,
            BigDecimal amount
    ) {
        breakdown.add(new PriceBreakdownItem(description, money(amount)));
    }

    private BigDecimal percentage(
            BigDecimal amount,
            BigDecimal rate
    ) {
        return money(amount.multiply(rate));
    }

    private BigDecimal money(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP);
    }
}
