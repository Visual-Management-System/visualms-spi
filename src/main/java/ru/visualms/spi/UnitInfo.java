package ru.visualms.spi;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * A unit (объект аренды).
 *
 * @param floor      the floor's name; null — no floor
 * @param purpose    its type (назначение); null — not set
 * @param area       the rentable area, m²
 * @param let        a lease is in force on {@link ReportData#day()}
 * @param activeFrom the day the unit was created; null — not known. Since 1.2
 * @param activeTo   the day it was archived; null — not archived. Since 1.2
 */
public record UnitInfo(UUID id, UUID buildingId, String name, String floor, String purpose, BigDecimal area,
                       boolean let, LocalDate activeFrom, LocalDate activeTo) {

    /** The fields of 1.0 / 1.1. */
    public UnitInfo(UUID id, UUID buildingId, String name, String floor, String purpose, BigDecimal area, boolean let) {
        this(id, buildingId, name, floor, purpose, area, let, null, null);
    }

    /** The unit existed on the day (an unknown start counts as always). Since 1.2 */
    public boolean existedOn(LocalDate day) {
        return (activeFrom == null || !activeFrom.isAfter(day)) && (activeTo == null || activeTo.isAfter(day));
    }
}
