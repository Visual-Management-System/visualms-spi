package ru.visualms.spi;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * An open deal (a lease being negotiated) of the scope: what may soon be let. Since 1.2
 *
 * @param stage       the step of the deal's funnel (its name); null — not set
 * @param unitIds     the units offered in the deal; empty — none chosen yet
 * @param buildingIds the buildings of the deal
 * @param areaFrom    / areaTo the area wanted, m²; null — not given
 * @param rentRate    the rent the tenant budgets, per m² a month in the base currency; null — not given, or the viewer
 *                    does not see {@link Finance#RENT_TERMS}
 * @param nextContact the next step's date; null — none
 * @param createdOn   the day the deal was opened
 */
public record DealInfo(UUID id, String name, String stage, List<UUID> unitIds, List<UUID> buildingIds,
                       BigDecimal areaFrom, BigDecimal areaTo, BigDecimal rentRate, LocalDate nextContact,
                       LocalDate createdOn) {
}
