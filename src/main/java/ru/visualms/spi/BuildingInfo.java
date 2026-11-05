package ru.visualms.spi;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * A building of the report.
 *
 * @param grossArea the building's gross area (GBA), m², as entered on its card; null — not entered. Since 1.4
 */
public record BuildingInfo(UUID id, String name, BigDecimal grossArea) {

    /** The fields of 1.0–1.3. */
    public BuildingInfo(UUID id, String name) {
        this(id, name, null);
    }
}
