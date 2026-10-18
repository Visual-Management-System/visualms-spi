package ru.visualms.spi;

import java.time.LocalDate;
import java.util.UUID;

/**
 * What the viewer chose for one metric on the dashboard; null — the default of the metric (the widget's months,
 * today, no filter).
 *
 * @param months  the widget's period, months back from the current one
 * @param from    the first month of a span (first day)
 * @param to      the last month of a span (first day), may lie ahead
 * @param at      the day «as of» (a DATE metric) or a day of the month (a MONTH metric)
 * @param days    a series by days instead of months
 * @param measure COUNT, AREA or PERCENT
 * @param floorId / leaseId public ids; kindId (a kind of fixed payment), purposeId, industryId — dictionary ids
 */
public record MetricQuery(Integer months, LocalDate from, LocalDate to, LocalDate at, boolean days, String measure,
                          UUID floorId, Long kindId, Long purposeId, Long industryId, UUID leaseId) {

    public static MetricQuery of(Integer months) {
        return new MetricQuery(months, null, null, null, false, null, null, null, null, null, null);
    }

    /** Narrows the units: by floor or purpose. */
    public boolean narrowsUnits() {
        return floorId != null || purposeId != null;
    }

    /** Narrows the leases: by lease or industry. */
    public boolean narrowsLeases() {
        return leaseId != null || industryId != null;
    }

    public boolean filtered() {
        return narrowsUnits() || narrowsLeases() || kindId != null;
    }
}
