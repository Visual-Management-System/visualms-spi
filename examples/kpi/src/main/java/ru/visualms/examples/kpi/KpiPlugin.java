package ru.visualms.examples.kpi;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.visualms.spi.Finance;
import ru.visualms.spi.LeaseInfo;
import ru.visualms.spi.LeaseInfo.EndReason;
import ru.visualms.spi.MetricCalculator;
import ru.visualms.spi.MetricDescriptor;
import ru.visualms.spi.MetricDescriptor.Filter;
import ru.visualms.spi.MetricDescriptor.Range;
import ru.visualms.spi.MetricDescriptor.Scope;
import ru.visualms.spi.MetricDescriptor.Shape;
import ru.visualms.spi.MetricDescriptor.Unit;
import ru.visualms.spi.MetricQuery;
import ru.visualms.spi.MetricResult;
import ru.visualms.spi.MetricSource;
import ru.visualms.spi.ReportData;
import ru.visualms.spi.VisualmsPlugin;

/**
 * An example plugin on SPI 1.2: WAULT by area and by income, the renewal rate of the last 12 months and the aging of
 * the debt. It shows how a plugin reads leases ({@link LeaseInfo}) and raw values with a bucket.
 */
public class KpiPlugin implements VisualmsPlugin, MetricSource, MetricCalculator {

    static final String WAULT_AREA = "example-kpi.wault_area";
    static final String WAULT_INCOME = "example-kpi.wault_income";
    static final String RENEWAL_RATE = "example-kpi.renewal_rate_12m";
    static final String AGING = "example-kpi.aging";

    private static final Set<Scope> BOTH = Set.of(Scope.BUILDING, Scope.PORTFOLIO);

    @Override
    public String id() {
        return "example-kpi";
    }

    @Override
    public String title() {
        return "Пример: KPI портфеля";
    }

    @Override
    public String version() {
        return "1.0.0";
    }

    @Override
    public String vendor() {
        return "visualms";
    }

    @Override
    public List<MetricSource> metricSources() {
        return List.of(this);
    }

    @Override
    public List<MetricCalculator> calculators() {
        return List.of(this);
    }

    @Override
    public List<MetricDescriptor> metrics() {
        return List.of(
                metric(WAULT_AREA, "WAULT по площади", "Средневзвешенный оставшийся срок договоров, вес — площадь",
                        Shape.VALUE, Unit.YEARS, null),
                metric(WAULT_INCOME, "WAULT по доходу", "Средневзвешенный оставшийся срок, вес — фиксированные платежи месяца",
                        Shape.VALUE, Unit.YEARS, Finance.RENT_TERMS),
                metric(RENEWAL_RATE, "Продления за 12 мес.", "Продлено / закончилось в срок за 12 месяцев "
                        + "(продлением считаются ДС, автопролонгация и новый договор того же арендатора)",
                        Shape.VALUE, Unit.PERCENT, null),
                metric(AGING, "Старение долга", "Задолженность по сроку просрочки на дату", Shape.BREAKDOWN,
                        Unit.MONEY, Finance.BALANCE));
    }

    private static MetricDescriptor metric(String code, String title, String description, Shape shape, Unit unit,
                                           Finance finance) {
        return new MetricDescriptor(code, title, description, "Пример", BOTH, shape, unit, finance,
                shape.visualizations().get(0), false, null, Set.of(Filter.FLOOR, Filter.UNIT_PURPOSE), Range.DATE,
                Set.of());
    }

    @Override
    public Set<String> codes() {
        return Set.of(WAULT_AREA, WAULT_INCOME, RENEWAL_RATE, AGING);
    }

    @Override
    public MetricResult compute(String code, ReportData data, MetricQuery query) {
        LocalDate day = data.day();
        return switch (code) {
            case WAULT_AREA -> wault(data, day, false);
            case WAULT_INCOME -> wault(data, day, true);
            case RENEWAL_RATE -> renewalRate(data, day);
            case AGING -> aging(data);
            default -> MetricResult.hidden("Неизвестная метрика");
        };
    }

    /** Σ years left × weight / Σ weight over the leases in force; open-ended leases are left out. */
    static MetricResult wault(ReportData data, LocalDate day, boolean byIncome) {
        BigDecimal sum = BigDecimal.ZERO;
        BigDecimal weights = BigDecimal.ZERO;
        for (LeaseInfo l : data.leases()) {
            BigDecimal years = l.yearsLeft(day);
            BigDecimal weight = byIncome ? l.monthlyRent() : l.area();
            if (!l.activeOn(day) || years == null || weight == null || weight.signum() <= 0) {
                continue;
            }
            sum = sum.add(years.multiply(weight));
            weights = weights.add(weight);
        }
        if (weights.signum() == 0) {
            return byIncome && !data.sees(Finance.RENT_TERMS) ? MetricResult.hidden("Нет доступа к условиям договоров")
                    : MetricResult.value(null, Unit.YEARS.title(data.moneySign()), null);
        }
        return MetricResult.value(sum.divide(weights, 1, RoundingMode.HALF_UP), Unit.YEARS.title(data.moneySign()), null);
    }

    /** Renewed / (renewed + expired) among the leases that ended on their planned date in the last 12 months. */
    static MetricResult renewalRate(ReportData data, LocalDate day) {
        int renewed = 0;
        int ended = 0;
        for (LeaseInfo l : data.leases()) {
            if (l.to() == null || !l.to().isBefore(day) || l.to().isBefore(day.minusYears(1))) {
                continue;
            }
            if (l.endReason() == EndReason.RENEWED) {
                renewed++;
                ended++;
            } else if (l.endReason() == EndReason.EXPIRED) {
                ended++;
            }
        }
        if (ended == 0) {
            return MetricResult.value(null, "%", "За 12 месяцев договоры в срок не заканчивались");
        }
        BigDecimal pct = BigDecimal.valueOf(renewed * 100L).divide(BigDecimal.valueOf(ended), 1, RoundingMode.HALF_UP);
        return MetricResult.value(pct, "%", renewed + " из " + ended);
    }

    /** The debt by the core's overdue buckets, as of the day. */
    static MetricResult aging(ReportData data) {
        List<String> buckets = Filter.DEFAULT_BUCKETS;
        List<BigDecimal> values = new ArrayList<>();
        for (String b : buckets) {
            BigDecimal v = data.raw("overdue", Map.of(Filter.OVERDUE_BUCKET.key(), b), data.day());
            if (v == null) {
                return MetricResult.hidden("Нет доступа к задолженности");
            }
            values.add(v);
        }
        return MetricResult.series(Unit.MONEY.title(data.moneySign()), buckets,
                List.of(new MetricResult.Series("Задолженность", Unit.MONEY.title(data.moneySign()), values)));
    }
}
