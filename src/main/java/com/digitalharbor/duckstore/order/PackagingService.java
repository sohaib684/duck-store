
package com.digitalharbor.duckstore.order;

import com.digitalharbor.duckstore.duck.DuckSize;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PackagingService {

    public PackagingResult determinePackaging(
            DuckSize size,
            ShippingMode shippingMode
    ) {
        PackageType packageType = determinePackageType(size);

        List<String> protections = switch (shippingMode) {
            case LAND -> List.of("Polystyrene balls");

            case AIR -> packageType == PackageType.PLASTIC
                    ? List.of("Bubble-wrap bags")
                    : List.of("Polystyrene balls");

            case SEA -> List.of(
                    "Moisture-absorbing beads",
                    "Bubble-wrap bags"
            );
        };

        return new PackagingResult(packageType, protections);
    }

    private PackageType determinePackageType(DuckSize size) {
        return switch (size) {
            case XLARGE, LARGE -> PackageType.WOOD;
            case MEDIUM -> PackageType.CARDBOARD;
            case SMALL, XSMALL -> PackageType.PLASTIC;
        };
    }
}
