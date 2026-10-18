package ru.visualms.spi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;

/** The additions of 1.2: the older constructors still work, the helpers count as documented. */
class ReportDataContractTest {

    private static final LocalDate DAY = LocalDate.of(2026, 1, 1);

    @Test
    void theOlderLeaseConstructorLeavesTheNewFieldsEmpty() {
        LeaseInfo l = new LeaseInfo(UUID.randomUUID(), "1", "ООО", UUID.randomUUID(), UUID.randomUUID(), DAY,
                DAY.plusYears(1), BigDecimal.TEN);
        assertEquals(l.to(), l.plannedTo());
        assertEquals(LeaseInfo.EndReason.ACTIVE, l.endReason());
        assertEquals(Set.of(), l.renewals());
        assertEquals(List.of(), l.industries());
        assertNull(l.monthlyRent());
    }

    @Test
    void yearsLeftCountFromTheDayOrTheStart() {
        LeaseInfo running = lease(DAY.minusYears(1), DAY.plusDays(364));
        assertEquals(0, new BigDecimal("0.999316").compareTo(running.yearsLeft(DAY)));
        LeaseInfo ended = lease(DAY.minusYears(2), DAY.minusDays(1));
        assertEquals(0, BigDecimal.ZERO.compareTo(ended.yearsLeft(DAY)));
        LeaseInfo future = lease(DAY.plusYears(1), DAY.plusYears(1).plusDays(364));
        assertEquals(0, new BigDecimal("0.999316").compareTo(future.yearsLeft(DAY)));
        assertNull(lease(DAY, null).yearsLeft(DAY));
    }

    @Test
    void theOlderUnitConstructorKnowsNoDates() {
        UnitInfo u = new UnitInfo(UUID.randomUUID(), UUID.randomUUID(), "A-1", null, null, BigDecimal.ONE, false);
        assertTrue(u.existedOn(DAY));
        UnitInfo archived = new UnitInfo(u.id(), u.buildingId(), "A-1", null, null, BigDecimal.ONE, false,
                DAY.minusYears(1), DAY);
        assertFalse(archived.existedOn(DAY));
        assertTrue(archived.existedOn(DAY.minusDays(1)));
    }

    @Test
    void rawOnlyFiltersAreTheNewOnes() {
        for (MetricDescriptor.Filter f : MetricDescriptor.Filter.values()) {
            assertEquals(f == MetricDescriptor.Filter.OVERDUE_BUCKET || f == MetricDescriptor.Filter.SECURITY_KIND,
                    f.rawOnly(), f.name());
        }
        assertEquals(List.of("current", "1-7", "8-14", "15-31", "32+"), MetricDescriptor.Filter.DEFAULT_BUCKETS);
    }

    private static LeaseInfo lease(LocalDate from, LocalDate to) {
        return new LeaseInfo(UUID.randomUUID(), "1", "ООО", UUID.randomUUID(), UUID.randomUUID(), from, to,
                BigDecimal.TEN);
    }
}
