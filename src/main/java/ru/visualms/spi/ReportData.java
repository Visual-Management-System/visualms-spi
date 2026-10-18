package ru.visualms.spi;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

/**
 * What a plugin's metric may read: the scope of the report as the viewer sees it. Buildings, units and leases are
 * only the visible ones; money is only from the buildings where the viewer sees the part of the finances — elsewhere
 * a raw value is empty ({@code null}), never zero.
 */
public interface ReportData {

    MetricDescriptor.Scope scope();

    /** The buildings of the report: one, or the visible buildings of the working set. */
    List<BuildingInfo> buildings();

    /** The base currency of the instance (ISO code: RUB, EUR …) — every money amount of the data is in it. */
    String currency();

    /** Its sign for units of results («₽», «€»): write it instead of a sign of your own. */
    String moneySign();

    /** The day «as of»: today or the day the viewer chose. */
    LocalDate day();

    /** The viewer sees this part of the finances in at least one building; null — no money, always true. */
    boolean sees(Finance finance);

    /** The catalog of raw values. */
    List<RawValue> rawValues();

    /**
     * A raw value — the same numbers a formula of a report reads.
     *
     * @param filters by {@link MetricDescriptor.Filter#key()}: the name of a floor, kind of payment, purpose, industry
     *                or the number of a lease, e.g. {@code kind → «Базовая арендная плата»}
     * @param from    the first month of a {@link RawValue.Kind#FLOW}; ignored for a stock (it is as of {@link #day()})
     * @param to      the last month, inclusive
     * @return null — empty or hidden
     * @throws IllegalArgumentException an unknown value or filter
     */
    BigDecimal raw(String name, Map<String, String> filters, YearMonth from, YearMonth to);

    /**
     * A {@link RawValue.Kind#STOCK} as of another day than {@link #day()}: the debt a year ago, the let area at the end
     * of each month… The same filters and rights as {@link #raw(String, Map, YearMonth, YearMonth)}. Since 1.2
     *
     * @return null — empty or hidden
     * @throws IllegalArgumentException an unknown value or filter, or a flow
     */
    default BigDecimal raw(String name, Map<String, String> filters, LocalDate at) {
        throw new UnsupportedOperationException("the core does not give stocks as of another day (SPI 1.2)");
    }

    /** Units (объекты аренды) of the scope, narrowed by the viewer's filters. */
    List<UnitInfo> units();

    /** Leases of the scope, one per lease and unit, past and future too. */
    List<LeaseInfo> leases();

    /** Open deals of the scope's buildings: what may soon be let. Since 1.2 */
    default List<DealInfo> deals() {
        return List.of();
    }
}
