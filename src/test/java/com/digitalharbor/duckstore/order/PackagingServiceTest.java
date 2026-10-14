
package com.digitalharbor.duckstore.order;

import com.digitalharbor.duckstore.duck.DuckSize;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PackagingServiceTest {

    private final PackagingService packagingService =
            new PackagingService();

    @Test
    void largeDuckUsesWoodPackaging() {
        PackagingResult result = packagingService.determinePackaging(
                DuckSize.LARGE, ShippingMode.LAND);

        assertEquals(PackageType.WOOD, result.packageType());
        assertEquals(
                java.util.List.of("Polystyrene balls"),
                result.protectionTypes()
        );
    }

    @Test
    void mediumDuckUsesCardboardPackaging() {
        PackagingResult result = packagingService.determinePackaging(
                DuckSize.MEDIUM, ShippingMode.LAND);

        assertEquals(PackageType.CARDBOARD, result.packageType());
    }

    @Test
    void smallDuckUsesPlasticPackaging() {
        PackagingResult result = packagingService.determinePackaging(
                DuckSize.SMALL, ShippingMode.AIR);

        assertEquals(PackageType.PLASTIC, result.packageType());
        assertEquals(
                java.util.List.of("Bubble-wrap bags"),
                result.protectionTypes()
        );
    }

    @Test
    void seaShippingUsesBothProtectionTypes() {
        PackagingResult result = packagingService.determinePackaging(
                DuckSize.MEDIUM, ShippingMode.SEA);

        assertEquals(
                java.util.List.of(
                        "Moisture-absorbing beads",
                        "Bubble-wrap bags"
                ),
                result.protectionTypes()
        );
    }
}
