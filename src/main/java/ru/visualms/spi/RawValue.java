package ru.visualms.spi;

import java.util.Set;

/**
 * A raw value of the catalog — what formulas of reports and plugins compute from: areas, units, leases, schedules,
 * accruals, payments, debts, turnover, budget.
 *
 * @param name    in a formula, e.g. {@code accrued}
 * @param finance the part of the finances it needs; null — no money
 * @param filters what it may be narrowed by
 */
public record RawValue(String name, String title, String description, MetricDescriptor.Unit unit, Kind kind,
                       Finance finance, Set<MetricDescriptor.Filter> filters) {

    public enum Kind {
        /** As of a day: areas, counts, debts */
        STOCK,
        /** Summed over months: schedules, accruals, payments, turnover, budget */
        FLOW
    }
}
