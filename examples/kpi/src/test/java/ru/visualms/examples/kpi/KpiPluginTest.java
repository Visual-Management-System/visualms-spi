package ru.visualms.examples.kpi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import ru.visualms.spi.BuildingInfo;
import ru.visualms.spi.Finance;
import ru.visualms.spi.LeaseInfo;
import ru.visualms.spi.LeaseInfo.EndReason;
import ru.visualms.spi.LeaseInfo.RenewalKind;
import ru.visualms.spi.MetricDescriptor;
import ru.visualms.spi.MetricResult;
import ru.visualms.spi.RawValue;
import ru.visualms.spi.ReportData;
import ru.visualms.spi.UnitInfo;

class KpiPluginTest {

    private static final LocalDate DAY = LocalDate.of(2026, 1, 1);

    private static LeaseInfo lease(LocalDate from, LocalDate to, String area, String rent, EndReason reason) {
        return new LeaseInfo(UUID.randomUUID(), "1", "ООО", UUID.randomUUID(), UUID.randomUUID(), from, to,
                new BigDecimal(area), UUID.randomUUID(), null, to, reason,
                reason == EndReason.RENEWED ? Set.of(RenewalKind.NEW_LEASE) : Set.of(), null,
                rent == null ? null : new BigDecimal(rent), List.of(), null);
    }

    /** A scope of the given leases; «overdue» by bucket is 100 × the bucket's position. */
    private static ReportData data(List<LeaseInfo> leases) {
        return new ReportData() {
            public MetricDescriptor.Scope scope() {
                return MetricDescriptor.Scope.PORTFOLIO;
            }

            public List<BuildingInfo> buildings() {
                return List.of();
            }

            public String currency() {
                return "RUB";
            }

            public String moneySign() {
                return "₽";
            }

            public LocalDate day() {
                return DAY;
            }

            public boolean sees(Finance finance) {
                return true;
            }

            public List<RawValue> rawValues() {
                return List.of();
            }

            public BigDecimal raw(String name, Map<String, String> filters, YearMonth from, YearMonth to) {
                return null;
            }

            @Override
            public BigDecimal raw(String name, Map<String, String> filters, LocalDate at) {
                int i = MetricDescriptor.Filter.DEFAULT_BUCKETS.indexOf(filters.get("bucket"));
                return BigDecimal.valueOf(100L * (i + 1));
            }

            public List<UnitInfo> units() {
                return List.of();
            }

            public List<LeaseInfo> leases() {
                return leases;
            }
        };
    }

    @Test
    void waultWeighsTheYearsLeft() {
        // 2 years × 300 m² and 1 year × 100 m² → 1.75 (1.749 with 365.25-day years); by income 2 × 1000 and 1 × 3000 → 1.25
        ReportData d = data(List.of(
                lease(DAY.minusYears(1), DAY.plusYears(2).minusDays(1), "300", "1000", EndReason.ACTIVE),
                lease(DAY.minusYears(1), DAY.plusYears(1).minusDays(1), "100", "3000", EndReason.ACTIVE),
                lease(DAY.minusYears(1), null, "500", "5000", EndReason.ACTIVE)));
        BigDecimal byArea = new KpiPlugin().compute(KpiPlugin.WAULT_AREA, d, null).value();
        BigDecimal byIncome = new KpiPlugin().compute(KpiPlugin.WAULT_INCOME, d, null).value();

        assertEquals(new BigDecimal("1.7"), byArea);
        assertEquals(new BigDecimal("1.2"), byIncome);     // 1.249 years
    }

    @Test
    void renewalRateCountsTheLastTwelveMonths() {
        ReportData d = data(List.of(
                lease(DAY.minusYears(3), DAY.minusMonths(2), "10", null, EndReason.RENEWED),
                lease(DAY.minusYears(3), DAY.minusMonths(5), "10", null, EndReason.EXPIRED),
                lease(DAY.minusYears(3), DAY.minusMonths(6), "10", null, EndReason.TERMINATED_EARLY),
                lease(DAY.minusYears(3), DAY.minusYears(2), "10", null, EndReason.EXPIRED)));

        MetricResult r = new KpiPlugin().compute(KpiPlugin.RENEWAL_RATE, d, null);
        assertEquals(new BigDecimal("50.0"), r.value());
        assertEquals("1 из 2", r.note());
    }

    @Test
    void agingAsksTheCoreBuckets() {
        MetricResult r = new KpiPlugin().compute(KpiPlugin.AGING, data(List.of()), null);
        assertNotNull(r.series());
        assertEquals(MetricDescriptor.Filter.DEFAULT_BUCKETS, r.categories());
        assertEquals(new BigDecimal("500"), r.series().get(0).data().get(4));
    }
}
