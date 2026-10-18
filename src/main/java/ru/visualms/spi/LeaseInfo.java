package ru.visualms.spi;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * An approved lease of one unit: a lease of several units gives several. {@code from..to} is the whole time the
 * approved versions of the lease let the unit (since 1.2 amendments do not split it).
 *
 * @param number      the lease's number; null — not given yet
 * @param lessee      the lessee's name (several — joined by commas)
 * @param to          the last day; null — open-ended
 * @param area        the unit's area, m²
 * @param lesseeId    the (first) lessee; null — none known. Since 1.2
 * @param signedOn    the date of the lease document (its main version); null — not given. Since 1.2
 * @param plannedTo   the end the lease had before an early termination; {@code to} otherwise. Since 1.2
 * @param endReason   how the lease ends as of {@link ReportData#day()}. Since 1.2
 * @param renewals    how the lease was or is renewed (several may hold). Since 1.2
 * @param previousId  the lease of the same unit this one follows: the previous lease of the unit that ended at most
 *                    {@link #FOLLOW_DAYS} days before this one started; null — none. Since 1.2
 * @param monthlyRent the fixed payments of the unit in force in the month of {@link ReportData#day()}, without VAT;
 *                    null — none that month, or the viewer does not see {@link Finance#RENT_TERMS}. Since 1.2
 * @param industries  the lease's industry directions (names). Since 1.2
 * @param tenantType  the type of the lessee (anchor, medium…) by the settings of the instance; null — none set up.
 *                    Since 1.2
 */
public record LeaseInfo(UUID id, String number, String lessee, UUID unitId, UUID buildingId, LocalDate from,
                        LocalDate to, BigDecimal area, UUID lesseeId, LocalDate signedOn, LocalDate plannedTo,
                        EndReason endReason, Set<RenewalKind> renewals, UUID previousId, BigDecimal monthlyRent,
                        List<String> industries, TenantType tenantType) {

    /** How many days may lie between two leases of a unit for the later one to follow the earlier one. */
    public static final int FOLLOW_DAYS = 90;

    /** The fields of 1.0 / 1.1; the rest is empty. */
    public LeaseInfo(UUID id, String number, String lessee, UUID unitId, UUID buildingId, LocalDate from, LocalDate to,
                     BigDecimal area) {
        this(id, number, lessee, unitId, buildingId, from, to, area, null, null, to, EndReason.ACTIVE, Set.of(), null,
                null, List.of(), null);
    }

    /** How a lease ends. Since 1.2 */
    public enum EndReason {
        /** Not ended by the day of the report (or not started yet) */
        ACTIVE,
        /** Ended on its planned date and the lessee did not go on in the unit */
        EXPIRED,
        /** Ended on its planned date and the same lessee goes on in the unit under a new lease */
        RENEWED,
        /** Ended earlier than planned (a termination amendment) */
        TERMINATED_EARLY
    }

    /** How a lease is renewed. Since 1.2 */
    public enum RenewalKind {
        /** An approved amendment extended the term */
        AMENDMENT,
        /** The lease renews itself: auto-renewal is on and no notice of non-renewal was given */
        AUTO,
        /** The same lessee goes on in the unit under a new lease */
        NEW_LEASE
    }

    /** A type of lessee (anchor, medium, small…) as the instance defines it. Since 1.2 */
    public record TenantType(String code, String name) {
    }

    /** In force on the day. */
    public boolean activeOn(LocalDate day) {
        return !from.isAfter(day) && (to == null || !to.isBefore(day));
    }

    /**
     * The term left after the day in years (365.25 days): the base of WAULT. 0 — ended; null — open-ended. A lease
     * not started yet counts from its start. Since 1.2
     */
    public BigDecimal yearsLeft(LocalDate day) {
        if (to == null) {
            return null;
        }
        LocalDate start = from.isAfter(day) ? from : day;
        long days = ChronoUnit.DAYS.between(start, to) + 1;
        return days <= 0 ? BigDecimal.ZERO
                : BigDecimal.valueOf(days).divide(BigDecimal.valueOf(36525, 2), 6, RoundingMode.HALF_UP);
    }
}
