
package com.digitalharbor.duckstore.order;

public enum PackageType {
    WOOD("Wood"),
    CARDBOARD("Cardboard"),
    PLASTIC("Plastic");

    private final String displayName;

    PackageType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
