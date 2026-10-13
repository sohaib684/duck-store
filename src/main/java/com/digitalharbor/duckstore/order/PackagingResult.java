
package com.digitalharbor.duckstore.order;

import java.util.List;

public record PackagingResult(
        PackageType packageType,
        List<String> protectionTypes
) {
}

